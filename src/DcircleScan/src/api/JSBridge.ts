import {Json} from "../3rdparty/ts-json";


let seq:number = 10;
interface Waiter<T> {
  seq:number
  resolve:(value:T)=>void
  reject:(err:Error) => void
  resType:{new(...args:any[]):T}|T
}

const allWaiters:Map<number, Waiter<any>> = new Map<number, Waiter<any>>([])
export async function ExeNativeMethod<T extends {[P in keyof T]:T[P]}>(method:string, request: object
   , resType:{new(...args:any[]):T}|T): Promise<[T, Error|null]> {
  if (typeof resType === "function") {
    resType = new resType()
  }

  const jsBridge = (window as any).DCircleBridge

  if (typeof (window as any).DCircleBridge.ExeNativeMethod === 'undefined' ||
    typeof (window as any).DCircleBridge.ExeNativeMethod !== 'function') {
    return [resType, Error("ExeNativeMethod not found")]
  }

  interface Request {
    seq:number
    method:string
    data:string
  }

  const cmd:Request = {
    seq:++seq,
    method:method,
    data:new Json().toJson(request),
  }

  const promise = new Promise<T>((resolve, reject) => {
    const waiter:Waiter<T> = {
      seq:cmd.seq,
      resType:resType,
      resolve:resolve,
      reject:reject,
    }
    allWaiters.set(cmd.seq, waiter);
  })

  if ((window as any).ExeNativeMethodCallback===undefined) {
    enum Code {
      Ok=200,
      Fail=500,
    }

    class Response {
      seq:number = 0
      data:string = ""
      method:string = ""
      code:number = Code.Ok
    }

    (window as any).ExeNativeMethodCallback = (response:string) => {
      console.log("ExeNativeMethodCallback", "response", typeof response, response)
      const [js, err] = new Json().fromJson(response, Response)
      console.log(js, '1234567890', err)
      if (err!=null) {
        console.error(`ExeNativeMethodCallback js decode err=${err}`)
        return;
      }

      if (!allWaiters.has(js.seq)) {
        console.warn("ExeNativeMethodCallback", `waiter seq=${js.seq} not found`)
        return;
      }

      console.log("ExeNativeMethodCallback", "js.data", js.data)
      const waiter = allWaiters.get(js.seq)!
      allWaiters.delete(js.seq)
      const [res, err1] = new Json().fromJson(js.data, waiter.resType)
      if (err1!=null) {
        waiter.reject(err1)
        return;
      }

      waiter.resolve(res)
    }
  }

  jsBridge.ExeNativeMethod(new Json().toJson(cmd))

  try {
    return [await promise, null]
  } catch (e:any) {
    return [resType, Error(e.message)]
  }
}

export async function AddDownloadTask(request: {objectId:string}):Promise<Error|null> {
  class Response {
    objectId:string = ""
  }

  const [ret, err] = await ExeNativeMethod("AddDownloadTask", request, Response)
  return err
}

export async function ReadDBFile(request:{objectId:string, key:string}):Promise<[string, Error|null]> {
  class Response {
    content:string = ""
  }

  const [ret, err] = await ExeNativeMethod("ReadDBFile", request, Response)
  if (err!=null) {
    return ["", err]
  }

  return [ret.content, null]
}

class GetIsJoinGroupResponse {
  isJoined:boolean = false
}
// 获取app中是否已经加群 true false
export async function GetIsJoinGroup(request: {groupId:string}):Promise<[GetIsJoinGroupResponse, Error|null]> {
  const [ret, err] = await ExeNativeMethod("GetIsJoinGroup", request, GetIsJoinGroupResponse)
  if (err) {
    return [new GetIsJoinGroupResponse(), err]
  }
  return [ret, null]
}
// 获取did是否已经购买
export async function GetIsBuyDid(request: {didAddress:string}):Promise<boolean> {
  class Response {
    isBuyDid: boolean = false
  }
  const [ret, err] = await ExeNativeMethod("GetIsBuyDid", request, Response)
  if (err) {
    return false
  }
  return ret.isBuyDid
}
// 定位到具体消息位置
export async function GotoChatMessage(request: {chatId:string, msgId: string}) {
  class Response {}
  const [ret, err] = await ExeNativeMethod("GotoChatMessage", request, Response)
  if (err) {
    return false
  }
  return true
}

export async function RunChat(request: {chatId: string}):Promise<boolean> {
  class Response {
    result:boolean = false;
  }
  const [ret, err] = await ExeNativeMethod("RunChat", request, Response)
  if (err) {
    return false
  }
  return true
}



// 跳转页面
export async function NavigateTo(request: {path:string, query: string}):Promise<Error|null> {
  class Response {
    result:boolean = false;
  }
  const [ret, err] = await ExeNativeMethod("NavigateTo", request, Response)
  return null;
}

