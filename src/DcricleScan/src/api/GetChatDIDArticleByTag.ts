import {ImageAttachment} from "../helper/Message";
import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

export class GetChatDIDArticleByTagRequest {
  chatId:string = ''
  tagId: string = ''
  pageIndex: number = 0 // 从0开始
  pageSize: number = 10
}

export class ChatDIDArticleItem {
  didAddress:string = ""
  title:string = ""
  abstract:string = ""
  abstractImage:ImageAttachment = new ImageAttachment()
  createTime:number = 0
  creatorAddress:string = ""
  creatorName:string = ""
  updateTime:number = 0
  msgId:string = ''
  nodupId:string = ''
}

class GetChatDIDArticleByTagResponse {
  items: ClassArray<ChatDIDArticleItem> = new ClassArray<ChatDIDArticleItem>(ChatDIDArticleItem)
}

export async function GetChatDIDArticleByTag(request: GetChatDIDArticleByTagRequest):Promise<[GetChatDIDArticleByTagResponse, Error|null]> {
  return await Send("/browser/GetChatDIDArticleByTag", request, GetChatDIDArticleByTagResponse);
}

