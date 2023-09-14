import {NewClient} from "./3rdparty/ts-streamclient-browser";
import {Client} from "./3rdparty/ts-streamclient-base";
import {StreamClient} from "./3rdparty/ts-baselib";
import {NcEvent} from "./3rdparty/ts-nc";

export class Streamer implements StreamClient {
  private client:Client
  constructor(private baseUrl:string)  {
    this.client = NewClient(baseUrl)
    this.client.setPeerClosedCallback(async () => {
      console.warn("PeerClosedCallback recovering")
      const err = await this.client.recover();
      if (err!=null) {
        console.warn("PeerClosedCallback recover failed", err);
        return
      }
    })
  }

  async send(content: string, uri: string, headers?: Map<string, string>): Promise<[string, (Error | null)]> {
    if (!headers) {
      headers = new Map<string, string>([]);
    }
    headers.set("api", uri);

    const [ret, err] = await this.client.send(content, headers);
    return [ret.toString(), err];
  }

  setPusher(push: (data: string) => void): void {
    this.client.setPushCallback(res => {
      console.log(`push callback ${res.toString()}`);
    });
  }


}

export class StreamDisconnected extends NcEvent<string> {
  static sym = Symbol();
}
export class StreamConnected extends NcEvent<string> {
  static sym = Symbol();
}
