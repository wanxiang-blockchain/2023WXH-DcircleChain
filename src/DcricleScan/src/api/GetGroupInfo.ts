import {Send} from "./api";

export class AvatarInfo {
  objectId: string = '';
  key: string = '';
}

export class GetGroupInfoResponse {
  address: string = ""; // 就是chatId
  memberNums: number = 0;
  contentNums: number = 0;
  chatId: string = "";
  status: number = -1;
  createTime: number = 0;
  creatorAddress: string = "";
  name:string = '';
  avatar:AvatarInfo = new AvatarInfo();
  avatarOnChainTxId:string = '';
  nameOnChainTxId:string = "";
  essenceKey:string = '';
  maxMemberNums: number = 0;
  joinedTotalMemberNums: number = 0;
}

export class GetGroupInfoRequest {
  address: string = "";
}
export async function GetGroupInfo(request:GetGroupInfoRequest):Promise<[GetGroupInfoResponse, Error|null]> {
  return await Send("/browser/GetGroupInfo", request, GetGroupInfoResponse);
}
