import {Send} from "./api";
import {IdType} from "../pages/qr/addressFactory";
export interface Context {
  idType: IdType;
  didAddress: string;
  uid: string;
  groupId: string;
}
export class GetShortLinkInfoByIDResponse {
  shortId: string = "";
  outTradeNo: string = "";
  context: string = "";
  txId: string = "";
}

export class GetShortLinkInfoByIDRequest {
  shortId: string = "";
}
export async function GetShortLinkInfoByID(request:GetShortLinkInfoByIDRequest):Promise<[GetShortLinkInfoByIDResponse, Error|null]> {
  const res = await Send("/im/chat/GetShortLinkInfoByID", request, GetShortLinkInfoByIDResponse);
  return res;
}