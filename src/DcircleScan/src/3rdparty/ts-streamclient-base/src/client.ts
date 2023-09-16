
import {Request, Response, Status} from "./fakehttp";
import {Net} from "./net"
import {option, Option} from "./option"
import {Millisecond} from "../../ts-xutils"
import {CloseEvent} from "./connection"
import {ConnError} from "./connerror"
import {Utf8} from "./utf8"

export class Result {
  public toString():string {
    return this.utf8.toString()
  }

  public rawBuffer():Uint8Array {
    return this.utf8.raw
  }

  constructor(private utf8:Utf8) {
  }
}

let emptyResult = new Result(new Utf8(""))

export class Client {
  private readonly net: Net;
  private allReq: Map<number, (result: {res: Response, err: null}|{res: null, err: Error}) => void>;
  private reqId: number;
  // private onPush: (res:string)=>Promise<void> = (res:string)=>{return Promise.resolve()};
  // private onPeerClosed: ()=>Promise<void> = ()=>{return Promise.resolve()};
  private onPush: (res:Result)=>void = ()=>{};
  private onPeerClosed: ()=>void = ()=>{};
  private op = new option

  // ws or wss 协议。
  constructor(wss: string, ...opf: Option[]) {
    if (wss.indexOf("s://") === -1) {
      wss = "ws://" + wss;
    }

    for (let o of opf) {
      o(this.op)
    }

    this.net = new Net(wss, this.op.connectTimeout, this.op.webSocketConstructor, {
      onMessage: (value: ArrayBuffer): void => {
        let res = new Response(value);
        if (res.isPush()) {
          // push ack 强制写给网络，不计入并发控制
          this.net.WriteForce(res.newPushAck())
          // 异步执行
          setTimeout(()=>{
            this.onPush(new Result(new Utf8(res.data())))
          }, 0)

          return;
        }

        let clb = this.allReq.get(res.reqID()) || (() => {});
        this.net.receivedOneResponse()
        clb({res:res, err:null});
        this.allReq.delete(res.reqID());

      }, onClose: (result: CloseEvent): void => {
        this.allReq.forEach((value) => {
          value({res:null, err: new ConnError(new Error("closed by peer: " + JSON.stringify(result)))})
        });
        this.allReq.clear()

        // 异步执行
        setTimeout(()=>{
          this.onPeerClosed()
        }, 0)
      }
    });

    // start from 10
    this.reqId = 10;
    this.allReq = new Map();
  }

  public updateWss(wss: string) {
    if (wss.indexOf("s://") === -1) {
      wss = "ws://" + wss;
    }
    this.net.updateWss(wss)
  }

  public setPushCallback(clb :(res:Result)=>void) {
    this.onPush = clb;
  }

  public setPeerClosedCallback(clb :()=>void) {
    this.onPeerClosed = clb;
  }

  public async send(data: ArrayBuffer | string, header?: Map<string, string>)
    : Promise<[Result, Error | null]> {

    let err = await this.net.Connect();
    if (err != null) {
      return [emptyResult, new ConnError(err)];
    }

    let req = new Request(data, header);
    let reqId = this.reqId++;
    req.SetReqId(reqId);

    let timer:number|undefined
    let res = new Promise<[Result, Error | null]>(
      (resolve: (ret: [Result, Error | null ]) => void) => {
        this.allReq.set(reqId, (result)=>{
          if (timer) {
            clearTimeout(timer)
          }

          if (result.err !== null) {
            resolve([emptyResult, result.err]);
            return
          }

          let res = result.res
          if (res.status !== Status.Ok) {
            resolve([emptyResult, new Error(new Utf8(res.data()).toString())]);
            return
          }

          resolve([new Result(new Utf8(res.data())), null]);
        });

        timer = setTimeout(()=>{
          this.allReq.delete(reqId)
          resolve([emptyResult, new Error("timeout")]);
        }, this.op.requestTimeout/Millisecond)as unknown as number;
      })

    err = await this.net.Write(req.ToData());
    // 向网络写数据失败，也应该归为连接层的错误
    if (err != null) {
      this.allReq.delete(reqId)
      if (timer) {
        clearTimeout(timer)
      }
      return [emptyResult, new ConnError(err)];
    }

    return res
  }

  public async recover(): Promise<Error|null> {
    return this.net.Connect();
  }
}

