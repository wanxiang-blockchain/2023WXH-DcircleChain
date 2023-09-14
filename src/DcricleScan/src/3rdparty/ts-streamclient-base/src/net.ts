import {Duration, Millisecond} from "../../ts-xutils"
import {Connection, MessageEvent, CloseEvent, ErrorEvent, WebSocketConstructor} from "./connection"


interface NetHandle {
  onMessage(value: ArrayBuffer): void;

  onClose(result: CloseEvent): void

  onError?: () => void
}

export class Net {

  private conn: Connection | null = null;
  private connected: boolean = false;
  private waitingConnect: Array<(ret: Error | null) => void> = new Array<(ret: Error | null) => void>();

  constructor(private wss: string, private connectTimeout: Duration
              , private webSocketConstructor: WebSocketConstructor
              , private handle: NetHandle) {
  }

  private doWaitingConnect(err: Error | null) {
    for (let waiting of this.waitingConnect) {
      waiting(err)
    }
    this.waitingConnect = new Array<(ret: Error | null) => void>();
  }

  private invalidWebsocket() {
    this.conn!.onmessage = () => {}
    this.conn!.onopen = () => {}
    this.conn!.onclose = () => {}
    this.conn!.onerror = () => {}
    this.conn = null;
  }

  public updateWss(wss: string) {
    this.wss = wss
  }

  // 采用最多只有一条连接处于活跃状态的策略（包括：connecting/connect/closing)，连接的判读可以单一化，对上层暴露的调用可以简单化。
  // 但对一些极限操作可能具有滞后性，比如正处于closing的时候(代码异步执行中)，新的Connect调用不能立即连接。为了尽可能的避免这种情况，
  // 在onerror 及 onclose 中都使用了同步代码。
  // 后期如果采用多条活跃状态的策略(比如：一条closing，一条connecting)，需要考虑net.handle的定义及异步情况的时序问题。
  public async Connect(): Promise<Error | null> {
    if (this.connected) {
      return null
    }

    return new Promise<Error | null>((resolve: (ret: Error | null) => void) => {
      this.waitingConnect.push(resolve);
      if (this.conn != null) {
        return
      }

      let timer = setTimeout(()=>{
        // invalid this.websocket
        this.invalidWebsocket()
        this.connected = false;

        this.doWaitingConnect(new Error("connect timeout"))
      }, this.connectTimeout/Millisecond)

      try {
        this.conn = new Connection(this.wss, this.webSocketConstructor);
      }catch (e) {
        // 目前观测到：1、如果url写错，则是直接在new就会抛出异常；2、如果是真正的连接失败，则会触发onerror，同时还会触发onclose
        console.error(e)
        this.conn = null;
        this.connected = false;
        clearTimeout(timer)
        this.doWaitingConnect(new Error(e as string))
        return
      }

      this.conn.onmessage = (result: MessageEvent)=>{
        this.handle.onMessage(result.data)
      };
      this.conn.onopen = () => {
        this.connected = true;
        clearTimeout(timer)
        this.doWaitingConnect(null);
      };
      this.conn.onclose = (result: CloseEvent) => {
        // 此处只考虑还处于连接的情况，其他情况可以参见 onerror的处理
        if (!this.connected) {
          return
        }

        let closeEvent = {code:result.code, reason: result.reason}
        if (closeEvent.reason === "" || closeEvent.reason === undefined || closeEvent.reason === null) {
          closeEvent.reason = "unknown"
        }
        console.warn("net---onClosed, ", JSON.stringify(closeEvent));
        this.handle.onClose(closeEvent);
        this.conn?.close();
        this.conn = null;
        this.connected = false;
      };

      this.conn.onerror = (result: ErrorEvent) => {
        console.error("net---onError", result);
        // 需要考虑连接失败的防御性代码，websocket接口没有明确指出连接失败由哪个接口返回，故这里加上连接失败的处理
        // 目前观测到：1、如果url写错，则是直接在new就会抛出异常；2、如果是真正的连接失败，则会触发onerror，同时还会触发onclose

        // 没有开始连接或者其他任何情况造成this.conn被置为空，都直接返回
        if (this.conn === null) {
          return
        }

        // 响应了onerror 就不再响应onclose
        this.conn.onclose = ()=>{}

        // 目前做如下的设定：一个上层的pending调用(连接或者请求等)，要么是在等待连接中
        // 要么是在等待response中。即使出现异常，上层一般可能都有超时，仍不会一直被pending
        // todo: 是否会有同时出现在 等连接 与 等响应 中？
        if (!this.connected) {
          clearTimeout(timer)
          this.doWaitingConnect(new Error(result.errMsg));
        } else {
          this.handle.onClose({code: -1, reason: "onerror: " + result.errMsg});
          if (this.handle.onError) {
            this.handle.onError();
          }
        }

        this.conn?.close();
        this.conn = null;
        this.connected = false;
      };

    });
  }

  public Write(data: ArrayBuffer): Error | null {
    if (this.conn == null || !this.connected) {
      return new Error("not connected")
    }

    return this.conn.send(data)
  }

  public WriteForce(data: ArrayBuffer) {
    this.conn?.SendForce(data)
  }

  public receivedOneResponse():void {
    this.conn?.receivedOneResponse()
  }

}