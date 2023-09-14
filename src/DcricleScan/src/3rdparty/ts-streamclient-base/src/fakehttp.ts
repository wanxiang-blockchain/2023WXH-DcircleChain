
/**

 content protocol:
   request ---
     reqid | headers | header-end-flag | data
     reqid: 4 bytes, net order;
     headers: < key-len | key | value-len | value > ... ;  [optional]
     key-len: 1 byte,  key-len = sizeof(key);
     value-len: 1 byte, value-len = sizeof(value);
     header-end-flag: 1 byte, === 0;
     data:       [optional]

      reqid = 1: client push ack to server.
            ack: no headers;
            data: pushId. 4 bytes, net order;

 ---------------------------------------------------------------------
   response ---
     reqid | status | data
     reqid: 4 bytes, net order;
     status: 1 byte, 0---success, 1---failed
     data: if status==success, data=<app data>    [optional]
     if status==failed, data=<error reason>


    reqid = 1: server push to client
        status: 0
          data: first 4 bytes --- pushId, net order;
                last --- real data

 */

import {Utf8} from "./utf8";

export class Request {
  private readonly buffer: ArrayBuffer;

  constructor(data:ArrayBuffer|string, header?:Map<string,string>) {
    let len = 4;
    header = header || new Map<string, string>();

    let headerArr = new Array<{key:Utf8, value:Utf8}>();

    header.forEach((value: string, key: string, _: Map<string, string>)=>{
      let utf8 = {key: new Utf8(key), value: new Utf8(value)};
      headerArr.push(utf8);
      len += 1 + utf8.key.byteLength + 1 + utf8.value.byteLength;
    });

    let body = new Utf8(data);

    len += 1 + body.byteLength;

    this.buffer = new ArrayBuffer(len);

    let pos = 4;
    for (let h of headerArr) {
      (new DataView(this.buffer)).setUint8(pos, h.key.byteLength);
      pos++;
      (new Uint8Array(this.buffer)).set(h.key.raw, pos);
      pos += h.key.byteLength;
      (new DataView(this.buffer)).setUint8(pos, h.value.byteLength);
      pos++;
      (new Uint8Array(this.buffer)).set(h.value.raw, pos);
      pos += h.value.byteLength;
    }
    (new DataView(this.buffer)).setUint8(pos, 0);
    pos++;

    (new Uint8Array(this.buffer)).set(body.raw, pos);
  }

  public SetReqId(id:number) {
    (new DataView(this.buffer)).setUint32(0, id);
  }

  public ToData():ArrayBuffer {
    return this.buffer
  }

}

export enum Status {
  Ok,
  Failed
}

export class Response {

  public readonly status: Status;
  private readonly buffer: Uint8Array;

  constructor(buffer: ArrayBuffer) {
    this.buffer = new Uint8Array(buffer);
    this.status = this.buffer[4] == 0?Status.Ok : Status.Failed;
  }

  public reqID():number {
    return (new DataView(this.buffer.buffer)).getUint32(0);
  }

  public data():ArrayBuffer {

    let offset = 5
    if (this.isPush()) {
      // pushId
      offset += 4
    }

    if (this.buffer.byteLength <= offset) {
      return new ArrayBuffer(0)
    }

    return this.buffer.slice(offset).buffer
    // let utf8 = new Utf8(this.buffer.slice(offset));
    // return utf8.toString();
  }

  public isPush():boolean {
    return this.reqID() === 1;
  }

  public newPushAck(): ArrayBuffer {
    if (!this.isPush() || this.buffer.byteLength <= 4+1+4) {
      return new ArrayBuffer(0)
    }

    let ret = new ArrayBuffer(4 + 1 + 4)
    let view = new DataView(ret)
    view.setUint32(0, 1)
    view.setUint8(4, 0)
    view.setUint32(5, (new DataView(this.buffer.buffer)).getUint32(5))

    return ret
  }

  public static fromError(reqId:number, err: Error):Response {
    let utf8 = new Utf8(err.message);
    let buffer = new Uint8Array(4+1 + utf8.byteLength);
    (new DataView(buffer.buffer)).setUint32(0, reqId);
    buffer[4] = 1;
    buffer.set(utf8.raw, 5);

    return new Response(buffer);
  }
}
