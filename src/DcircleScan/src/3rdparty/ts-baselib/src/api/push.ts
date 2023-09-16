import { Json, JsonObject, RawJson } from "../../../ts-json";

class Command {
  cmd: string = ""
  data: RawJson = new RawJson()
}

type NetName = string;
type Cmd = string;

interface Net {
  name: string
}

let allPushHandlers = new Map<NetName, Map<Cmd, (data: RawJson)=>void>>()

export function RegisterStreamPush<T>(net: Net, cmd: Cmd
        , handler: (data: T)=>void, clazz:{new (...args:any[]):T}) {

  console.info(`RegisterStreamPush ${cmd}` + " for " + net.name)
  let cmds = allPushHandlers.get(net.name)
  if (!cmds) {
    cmds = new Map<string, (data: RawJson)=>void>()
    allPushHandlers.set(net.name, cmds)
  }
  cmds.set(cmd, data => {
    let [res, err] = new Json().fromJson(data.raw as JsonObject, clazz)
    if (err) {
      console.error(err)
      return
    }
    handler(res)
  })
}

export function UnRegisterStreamPush(net: Net, cmd: Cmd) {
  console.info(`UnRegisterStreamPush ${cmd}` + " for " + net.name)
  let cmds = allPushHandlers.get(net.name)
  if (!cmds) {
    return
  }

  cmds.delete(cmd)
}

export function handlerOfPush(net:Net): (data: string)=>void{
  const baseUrl = net.name

  return data => {
    let cmds = allPushHandlers.get(baseUrl)
    if (!cmds) {
      console.warn("not register push handler for " + baseUrl)
      return
    }

    let [cmd, err] = new Json().fromJson(data, Command)
    if (err) {
      console.error(err)
      return;
    }
    if (cmd.cmd === null) {
      console.warn("'cmd' in push data is error for " + baseUrl)
      return
    }
    let handler = cmds.get(cmd.cmd)
    if (!handler) {
      console.warn(`not register push handler for ${cmd.cmd} of ${baseUrl}`)
      return
    }

    if (cmd.data === null) {
      console.warn(`'data' in push data is null for ${cmd.cmd} of ${baseUrl}`)
      return
    }

    handler(cmd.data)
  }
}


