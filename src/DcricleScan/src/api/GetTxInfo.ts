import {Send} from "./api";

export enum PayStatus {
  SUCCESS = 0
}

export class GetTxInfoResponse {
  txHash: string = "";
  payStatus: PayStatus = PayStatus.SUCCESS; // 2 Paid
  buyUserid: string = "";
  buySignData: string =  "";
  didAddress: string = "";
  creatorAddress: string = "";
  createSignData: string = "";
  txTime: number = 0;
  sourceChatId: string = "";
  transferId: string = "";
  transferSignData: string = "";
  mNums: number = 0;
  nonce: number = 0;
}

export class GetTxInfoRequest {
  id: string = "";
}
export async function GetTxInfo(request:GetTxInfoRequest):Promise<[GetTxInfoResponse, Error|null]> {
  return await Send("/browser/GetTxInfo", request, GetTxInfoResponse);
}