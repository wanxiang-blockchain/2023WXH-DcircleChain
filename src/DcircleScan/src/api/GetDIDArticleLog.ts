import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

export class GetDIDArticleLogRequest {
  address: string = "";
}

export enum UpdateType {
  UpdateTypeContent      = 1,
  UpdateTypeAbstract     = 2,
  UpdateTypeTokenAddress = 3
}

export class GetDIDArticleLogItem {
  verHash: string = "";
  updateType: UpdateType = UpdateType.UpdateTypeContent;
  updateTime: number = 0;
  version:number = 0;
}

class GetDIDArticleLogResponse {
  items: ClassArray<GetDIDArticleLogItem> = new ClassArray<GetDIDArticleLogItem>(GetDIDArticleLogItem);
}
export async function GetDIDArticleLog(request:GetDIDArticleLogRequest):Promise<[GetDIDArticleLogResponse, Error|null]> {
  return await Send("/browser/GetDIDArticleLog", request, GetDIDArticleLogResponse);
}