import {Http, HttpBuilder} from "./http"


export interface StreamClientConstructor {
  new (baseurl: string):StreamClient
}

export interface StreamClient {
  send(content:string, uri:string, headers?:Map<string, string>):Promise<[string, (Error | null)]>
  setPusher(push:(data:string)=>void):void
}

let Client:StreamClientConstructor

export function setStreamClientConstructor(con: StreamClientConstructor) {
  Client = con
}

class Stream implements Http{
  async send(): Promise<[string, (Error | null)]> {
    let client = Stream.allClients.get(this.builder.baseUrl())
    if (!client) {
      client = new Client(this.builder.baseUrl())
      Stream.allClients.set(this.builder.baseUrl(), client)
      client.setPusher(this.builder.pusher)
    }

    return client.send(this.builder.content(), this.builder.uri(), this.builder.headers())
  }

  constructor(private builder: HttpBuilder) {}

  private static allClients = new Map<string, StreamClient>()
}

export function StreamBuilderCreator(): (baseUrl:string)=>HttpBuilder {
  return baseUrl => {
    return new class extends HttpBuilder {
      build(): Http {
        return new Stream(this)
      }
    }(baseUrl)
  }
}

