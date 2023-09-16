import { RawJson, JsonObject, Json, JsonHas } from "../../../ts-json";
import {CodeError, PostJsonNoToken} from "./api";
import {Token} from "../db/token"
import {UserSpace} from "../userspace"

class LogRes {
  uid:string = ""
  token:string = ""
  data:RawJson = new RawJson()
}

interface LoginRequest {
  uri: string
  content: object
  headers?: Map<string, string>
}

export async function PostJsonLoginWithRes<T extends object>(req: LoginRequest, resType:{new(...args:any[]):T}
    , oldUs: UserSpace, netName?: string): Promise<[UserSpace, T, CodeError|null]>{

  let net = oldUs.nf.get(netName)

  await net.clearToken();

  let ret = new resType();

  let [res, err] = await PostJsonNoToken(req.uri, req.content, LogRes, net, req.headers)
  if (err) {
    return [oldUs, ret, err];
  }

  const has = JsonHas(res)

  if (!has.token || res.token === Token.Empty || !res.uid || res.uid === "") {
    return [oldUs, ret, new CodeError("token is null")];
  }

  let newUs = await oldUs.clone(res.uid, netName)
  net = newUs.nf.get(netName)
  await net.setToken(res.token);

  if (!res.data || !res.data.raw || typeof res.data.raw !== "object") {
    return [oldUs, ret, new CodeError("data format is error! must be {xxx}")];
  }

  const [retn, errn] = new Json().fromJson(res.data.raw as JsonObject, resType)
  if (errn !== null) {
    return [oldUs, ret, new CodeError(errn.message)]
  }

  return [newUs, retn, null];
}

class Response {}

export async function PostJsonLogin(req: LoginRequest, oldUs: UserSpace
                                    , netName?: string): Promise<[UserSpace, CodeError|null]> {
  let [us, _, err] = await PostJsonLoginWithRes(req, Response, oldUs, netName)
  return [us, err]
}
