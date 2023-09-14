
import {Client, WebSocket, Option} from "../ts-streamclient-base"
import {DomWebSocket} from "./src/websocket"

export function NewClient(wss: string, ...opf: Option[]): Client {
  opf.push(WebSocket(DomWebSocket))
  return new Client(wss, ...opf)
}
