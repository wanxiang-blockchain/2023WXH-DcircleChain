import {Net} from "./net";
import {PostJson} from "./api";

class Request {}
class Response {}

export async function PostJsonLogout(uri:string, net:Net
  , headers:Map<string, string> = new Map<string, string>()): Promise<void> {

  await PostJson(uri, new Request(), Response, net, headers)

  await net.clearToken();
}