import {Send} from "./api";

export class GetInviteCodeInfoResponse {
  groupId: string = ""
}

export class GetInviteCodeInfoRequest {
  inviteCode: string = "";
}
export async function GetInviteCodeInfo(request:GetInviteCodeInfoRequest):Promise<[GetInviteCodeInfoResponse, Error|null]> {
  const res = await Send("/browser/GetInviteCodeInfo", request, GetInviteCodeInfoResponse);
  return res;
}
