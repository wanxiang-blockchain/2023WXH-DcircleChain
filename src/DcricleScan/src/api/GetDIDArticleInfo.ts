import {Send} from "./api";

export class GetDIDArticleInfoResponse {
  address: string = "";
  title: string = "";
  abstract: string = "";
  token: string = "";
  version: number = 0;
  initCode: number = 0;
  salt: string = "";
  hash: string = "";
  createTime: number = 0;
  updateTime: number = 0;
  creatorAddress: string = "";
  featureVale:string[] = [];
}

export class GetDIDArticleInfoRequest {
  address: string = "";
  version: number = 0;
}
export async function GetDIDArticleInfo(request:GetDIDArticleInfoRequest):Promise<[GetDIDArticleInfoResponse, Error|null]> {
  return await Send("/browser/GetDIDArticleInfo", request, GetDIDArticleInfoResponse);
}