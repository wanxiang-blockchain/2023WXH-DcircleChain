
export interface Event {

}

export interface MessageEvent extends Event{
  readonly data: ArrayBuffer
}

export interface CloseEvent extends Event{
  readonly code: number;
  readonly reason: string;
}

export interface ErrorEvent extends Event{
  errMsg: string
}

export interface WebSocketInterface {
  onclose: ((this: WebSocketInterface, ev: CloseEvent) => any);
  onerror: ((this: WebSocketInterface, ev: ErrorEvent) => any);
  onmessage: ((this: WebSocketInterface, ev: MessageEvent) => any);
  onopen: ((this: WebSocketInterface, ev: Event) => any);

  close(code?: number, reason?: string): void;
  send(data: ArrayBuffer): void;
}

export interface WebSocketConstructor {
  new (url: string): WebSocketInterface
}

export class DummyWs implements WebSocketInterface{
  onclose = ()=>{}
  onerror = ()=>{}
  onmessage = ()=>{}
  onopen = ()=>{}

  close(): void {
  }

  send(): void {
    throw new Error("not set WebSocketConstructor")
  }
}

export class Connection {

  private maxConcurrent : number = 5;
  private maxBytes: number = 4 * 1024 * 1024;
  private connectID: string = "";

  public onclose: ((ev: CloseEvent) => any) = ()=>{};
  public onerror: ((ev: ErrorEvent) => any) = ()=>{};
  public onmessage: ((ev: MessageEvent) => any) = ()=>{};
  public onopen: ((ev: Event) => any) = ()=>{};

  private waitingSend = new Array<ArrayBuffer>()
  private concurrent = 0

  private websocket: WebSocketInterface;

  constructor(url: string, websocketConstructor: WebSocketConstructor) {
    this.websocket = new websocketConstructor(url)

    this.websocket.onclose = (ev: CloseEvent)=>{
      this.onclose(ev)
    }
    this.websocket.onerror = (ev: ErrorEvent)=>{
      this.onerror(ev)
    }
    this.websocket.onmessage = (result: MessageEvent)=>{
      let err = this.readHandshake(result)
      if (err != null) {
        console.error(err)
        this.websocket.onclose = ()=>{}
        this.websocket.onerror = ()=>{}
        this.websocket.onopen = ()=>{}
        this.websocket.onmessage = ()=>{}

        this.websocket.close();
        this.onerror({errMsg: err.message})

        return
      }

      // 设置为真正的接收函数
      this.websocket.onmessage = this.onmessage

      // 握手结束才是真正的onopen
      this.onopen({})
    }
    this.websocket.onopen = (_: Event)=>{
      // nothing to do
    }
  }

  /*
    HeartBeat_s | FrameTimeout_s | MaxConcurrent | MaxBytes | connect id
    HeartBeat_s: 2 bytes, net order
    FrameTimeout_s: 1 byte  ===0
    MaxConcurrent: 1 byte
    MaxBytes: 4 bytes, net order
    connect id: 8 bytes, net order
*/
  private readHandshake(result: MessageEvent): Error | null {
    let buffer = result.data
    if (buffer.byteLength != 16) {
      return new Error("len(handshake) != 16")
    }

    let view = new DataView(buffer);

    this.maxConcurrent = view.getUint8(3);
    this.maxBytes = view.getUint32(4);
    this.connectID = ("00000000" + view.getUint32(8).toString(16)).slice(-8) +
      ("00000000" + view.getUint32(12).toString(16)).slice(-8);
    console.log("connectID = ", this.connectID)

    return null
  }

  public receivedOneResponse():void {
    this.concurrent--
    // 防御性代码
    if (this.concurrent < 0) {
      console.warn("connection.concurrent < 0")
      this.concurrent = 0
    }

    this._send()
  }

  private _send():void {
    if (this.concurrent > this.maxConcurrent) {
      return
    }

    if (this.waitingSend.length == 0) {
      return
    }

    this.concurrent++

    this.websocket.send(this.waitingSend.shift()!)
  }

  public send(data: ArrayBuffer): Error | null {
    if (data.byteLength > this.maxBytes) {
      return new Error("data is too large! Must be less than " + this.maxBytes.toString() + ". ")
    }

    this.waitingSend.push(data)
    this._send()
    return null
  }

  public SendForce(data: ArrayBuffer) {
    this.websocket.send(data)
  }

  public close() {
    this.websocket.close()
  }
}
