import {Net} from "./net";
import {Token} from "../db/token"
import {Md5} from "ts-md5"
import {Json, JsonHas } from "../../../ts-json";

export enum Code {
  TokenExpireCode = 401,
  Unknown = -1,
  Success = 200
}

export class CodeError extends Error{
  constructor(err:string|Error, public code:Code = Code.Unknown) {
    super();
    this.name = "CodeError"
    if (typeof err === 'string') {
      this.message = `${err}, code:${code}`
    } else {
      this.message = `${err.message}, code:${code}`
      this.stack = err.stack
    }
  }
}

export const ReqId = "X-Req-Id"

export class Request {
  constructor(public token:string, public data:object) {
  }
}

export class Response<T> {
  constructor(public code:number, public data:T) {
  }
}

/*
 网络不能正常通信的情况：本地net为401 或者 token 为空，直接返回，不发起真正的网络请求
 token为空或者服务器告之网络不通的情况，net层需要process401。
 时序规定：
 api是401不一定net也是401，可能api返回时，其他地方已经处理完了401；
 net是401，不一定该api也是401，可能401发生在服务器返回此api之前；
 如果api是401，但net不是401，则一定是其他地方已经处理完了401，而不是没有处理401的情况，
    也就是本地第一个401的api，一定同时让net也处理401
*/
export async function PostJson<T extends object>(uri:string, request: object
  , resType:{new(...args:any[]):T}, net:Net, headers:Map<string, string> = new Map<string, string>())
  :Promise<[T, CodeError|null]> {

  if (net.is401()) {
    return [new resType(), new CodeError("has 401", Code.TokenExpireCode)]
  }
  let token = await net.getToken()
  // token为空，需要处理401，并且不执行真正的网络请求。
  if (token === Token.Empty) {
    net.process401()
    return [new resType(), new CodeError("token is empty", Code.TokenExpireCode)]
  }

  const r = new Request(token, request)

  let [ret, err] = await PostJsonNoToken(uri, r, new Response(0, new resType()), net, headers)
  if (err !== null) {
    return [new resType(), err]
  }

  const has = JsonHas(ret)

  if (!has.code || !has.data) {
    return [new resType(), new CodeError("response format is err!", Code.Unknown)]
  }

  if (ret.code !== Code.TokenExpireCode) {
    // 取消401
    net.clear401()
    return [ret.data, err]
  }

  // 返回401时，如果请求的token与现在存储token不是一样的，说明有登录接口修改过，最新
  // 存储的token是否过期，无法判断，所以不能执行真正的401操作
  // 此处不能通过net层is401来判断，还没有开始处理401或者其他接口处理完401的情况下，is401都是false
  // 但是后一种情况却不能执行401；对token而言后一种情况token一定不一样，却可以分辨出两种情况
  if (token !== await net.getToken()) {
    return [new resType(), new CodeError("token is too old", Code.TokenExpireCode)]
  }

  net.process401()
  return [new resType(), new CodeError("token is expire", Code.TokenExpireCode)]
}

export function random(min: number, max: number): number {
  min = Math.ceil(min);
  max = Math.floor(max);
  //The maximum is exclusive and the minimum is inclusive
  return Math.floor(Math.random() * (max - min)) + min;
}

export function SignNonceStr(len: number = 20): string {
  let str: string = Md5.hashStr(random(1000, 9999) + "" + random(10000, 99999)).toString();
  let rep: number = Math.floor(len / 32);
  let sup: number = len % 32;
  let res: string = "";
  for (let i: number = 0; i < rep; ++i) {
    res += str;
  }
  res += str.substring(0, sup);
  return res;
}

export async function PostJsonNoToken<T extends {[P in keyof T]:T[P]}>(uri:string, request: object
  , resType:{new(...args:any[]):T}|T, net:Net, headers:Map<string, string> = new Map<string, string>())
  : Promise<[T, CodeError|null]> {

  if (typeof resType === "function") {
    resType = new resType()
  }

  if (!headers.has(ReqId)) {
    headers.set(ReqId, SignNonceStr())
  }
  let reqid = headers.get(ReqId);

  console.log(`uri:${uri} reqid:${reqid} to ${net.getBaseUrl()}`);

  let req = new Json().toJson(request);

  console.log(`uri:${uri} reqid:${reqid} request`, req);

  let [res, err] = await net.getHttpBuilder().setUri(uri).setHeaders(headers)
    .setContent(req).build().send()
  if (err) {
    return [resType, new CodeError(err)]
  }
  if (res === "") {
    return [resType, new CodeError("response is empty")]
  }

  console.log(`uri:${uri} reqid:${reqid} response`, res);

  let [resT, err1] = new Json().fromJson(res, resType)
  if (err1) {
    return [resType, new CodeError(err1)]
  }

  return [resT, null]
}
