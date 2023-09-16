import {GetIsBuyDid, GetIsJoinGroup, GotoChatMessage, NavigateTo, RunChat} from "../api/JSBridge";

export namespace BusHelper {
  export enum PathEnum {
    imGroupMain = "imGroupMain", // 群简介
    didContent = "didContent", // did详情
    didSignature = "didSignature", // did未购买时的地址
    imChat = "imChat", // 聊天窗口
    imChatFileInfo = "imChatFileInfo", // 查看文件

  }
  function GetRealPath(pathKey: PathEnum) {
    const map = new Map([
      [PathEnum.imGroupMain, '/moudle_imchat/imchat/imGroupMain'],
      [PathEnum.didContent, "/moudle_did/did/didContent"],
      [PathEnum.imChat, "/moudle_imchat/imchat/imChat"],
      [PathEnum.didSignature, "/moudle_did/did/didSignature"],
      [PathEnum.imChatFileInfo, "/moudle_imchat/imchat/fileinfo"]
    ])
    if (!map.has(pathKey)) {
      console.error('can not find path by' + pathKey)
      return ''
    }
    return map.get(pathKey)!;
  }
  // didContent与app通信
  export async function HandleDidNcApp(groupId:string, didAddress:string, msgId:string, noDupId:string = ''):Promise<Error | null> {
    let realPath = GetRealPath(PathEnum.imGroupMain);
    if (realPath.length <= 0) {
      return new Error('can not find path by' + BusHelper.PathEnum.imGroupMain)
    }
    const [res, err] = await GetIsJoinGroup({groupId: groupId});
    if (err) {
      return new Error(`getIsJoinGroup is fail by groupId: ${groupId}`)
    };
    // 是否已经加入了群
    const query = `groupId=${groupId}&inviteCode=`;
    // 判断是否购买了did，跳转不同的页面
    const ret = await GetIsBuyDid({didAddress});
    const didPath = ret ? GetRealPath(PathEnum.didContent) : GetRealPath(PathEnum.didSignature);
    const queryDid = `param=${didAddress}&msgId=${msgId}&BuyId=${noDupId}`

    // 跳转到群简介
    await NavigateTo({path: realPath, query: query});
    return await NavigateTo({path: didPath, query: queryDid});
  }

  // voice内容与app通信
  export async function HandleVoiceNcApp(chatId:string, msgId: string) {
    let realPath = GetRealPath(PathEnum.imChat);
    if (realPath.length <= 0) {
      return new Error('can not find path by' + BusHelper.PathEnum.imChat)
    }
    const [res, err] = await GetIsJoinGroup({groupId: chatId});
    if (err) {
      return new Error(`getIsJoinGroup is fail by groupId: ${chatId}`);
    };
    // 是否已经加入了群
    const query = !res.isJoined ? `chatId=${chatId}&msgId=${msgId}` : `chatId=${chatId}&msgId=${msgId}&BuyId=`;
    const ret = await RunChat({chatId:chatId});
    // 定位到具体消息位置
    await GotoChatMessage({chatId:chatId, msgId: msgId})
  }

  // 快速定位按钮事件处理
  export async function HandleToChatMessage(chatId:string, msgId: string) {
    let realPath = GetRealPath(PathEnum.imChat);
    if (realPath.length <= 0) {
      return new Error('can not find path by' + BusHelper.PathEnum.imChat)
    }
    const [res, err] = await GetIsJoinGroup({groupId: chatId});
    if (err) {
      return new Error(`getIsJoinGroup is fail by groupId: ${chatId}`);
    };
    // 是否已经加入了群
    const query = !res.isJoined ? `chatId=${chatId}&msgId=${msgId}` : `chatId=${chatId}&msgId=${msgId}&BuyId=`;
    const ret = await RunChat({chatId:chatId});
    // 定位到具体消息位置
    await GotoChatMessage({chatId:chatId, msgId: msgId})
  }

  // file内容与app通信
  export async function HandleFileNcApp(chatId:string, msgId:string, objectId:string) {
    let realPath = GetRealPath(PathEnum.imChat);
    if (realPath.length <= 0) {
      return new Error('can not find path by' + BusHelper.PathEnum.imChat)
    }
    const [res, err] = await GetIsJoinGroup({groupId: chatId});
    if (err) {
      return new Error(`getIsJoinGroup is fail by groupId: ${chatId}`);
    };

    const query = !res.isJoined ? `chatId=${chatId}&msgId=${msgId}` : `chatId=${chatId}&msgId=${msgId}&BuyId=`;
    const ret = await NavigateTo({path: realPath, query: query});
    // 定位到具体消息位置
    await GotoChatMessage({chatId:chatId, msgId: msgId})
  }
}
