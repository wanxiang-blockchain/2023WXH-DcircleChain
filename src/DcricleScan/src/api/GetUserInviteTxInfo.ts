import {Send} from "./api";

export enum Status {
  SUCCESS = 1
}

export class GetUserInviteTxInfoResponse {

  id: string = "";
  txHash: string = "";
  inviteeUid: string = "";
  joinChatId: string =  "";
  joinTime: number = 0;
  invitorUid: string = "";
  inviteCodeTxHash: string = "";
  chatCreator: string = "";
  status: Status = Status.SUCCESS;
}

export class GetUserInviteTxInfoRequest {
  id: string = "";
}
export async function GetUserInviteTxInfo(request:GetUserInviteTxInfoRequest):Promise<[GetUserInviteTxInfoResponse, Error|null]> {
  return await Send("/browser/GetUserInviteTxInfo", request, GetUserInviteTxInfoResponse);
}