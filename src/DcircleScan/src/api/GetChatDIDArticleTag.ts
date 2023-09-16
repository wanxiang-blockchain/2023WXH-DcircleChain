import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

class GetChatDIDArticleTagRequest {
  chatId:string = '';
}

export class DidTag {
  id:string = ''
  name:string = ''
  count:number = 0
}

class GetChatDIDArticleTagResponse {
  items: ClassArray<DidTag> = new ClassArray<DidTag>(DidTag)
}

export async function GetChatDIDArticleTag(chatId:string):Promise<[GetChatDIDArticleTagResponse, Error|null]> {
  return await Send("/browser/GetChatDIDArticleTag", {chatId: chatId}, GetChatDIDArticleTagResponse);
}

