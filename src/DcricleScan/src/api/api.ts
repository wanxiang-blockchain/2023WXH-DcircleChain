import {PostJsonNoToken} from "../3rdparty/ts-baselib";
import {getUs} from "../DIDBrowser";
import {Code, Request, Response} from "../3rdparty/ts-baselib/src/api/api";


export async function Send<T extends object>(uri:string, req:object, resType:{new(...args:any[]):T}, headers:Map<string, string> = new Map()): Promise<[T, Error|null]> {
  const net = getUs().nf.get();

  const r = new Request("", req)
  const [ret, err] = await PostJsonNoToken(uri, r,   new Response(0, new resType()), net, headers);
  if (err) {
    console.warn("Send err", err);
    return [new resType(), new Error(err.message)];
  }

  if (ret.code !== Code.Success) {
    return [new resType(), new Error("code is not 200")];
  }

  return [ret.data, null];
}
