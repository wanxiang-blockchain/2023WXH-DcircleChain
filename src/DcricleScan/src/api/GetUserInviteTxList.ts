import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

export class GetUserInviteTxListResponseItem {
  txHash: string = "";
  inviteeUid: string = "";
  joinChatId: string = "";
  joinTime: number = 0;
}

export class GetUserInviteTxListResponse {
  public items:ClassArray<GetUserInviteTxListResponseItem> = new ClassArray<GetUserInviteTxListResponseItem>(GetUserInviteTxListResponseItem)
}

export class GetUserInviteTxListRequest {
  public address:string = "";
  public startTxTime:number = 0;

  public size:number = 10;
}

export async function GetUserInviteTxList(request:GetUserInviteTxListRequest,source: string = ''):Promise<[GetUserInviteTxListResponse, Error|null]> {
  const [ret, err] = await Send("/browser/GetUserInviteTxList", request, GetUserInviteTxListResponse);
  if (err) {
    return [ret, err];
  }
  // ret.id = `${ret.address}_${request.role}`
  // await DBDIDArticleRole.InsertOrUpdate(GetDScan(), ret);
  // await getUs().nc.post(DBDIDArticleRole.ChangedEvent.New([request.address], source))
  return [ret, err];
}

