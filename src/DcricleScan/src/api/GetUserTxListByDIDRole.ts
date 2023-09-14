import {Send} from "./api";
import {DIDRole} from "./DIDRole";
import {ClassArray} from "../3rdparty/ts-json";
import {GetDIDArticleBriefInfo} from "./GetDIDArticleBriefInfo";
import {GetGroupBriefInfo} from "./GetGroupBriefInfo";

export class GetUserTxListByDIDRoleResponseItem {
  id: string = "";
  address: string = "";
  txHash: string = "";
  buyUserId: string = "";
  buyTime: number = 0;
  didAddress: string = "";
  sourceChat: string = "";
  transferChat: string = "";
  transferUserId: string = "";
  tokenNums: number = 0;
  buyUserNotInChat:boolean = false;
  transferUserNotInChat: boolean = false;
  didNotInChat:boolean = false;
  buyDidBlockRootHash:string = '';
  createTime: number = 0;
  transferTime: number = 0;
  didContent:string = ""; // did摘要内容
  groupContent:string = ""; // 群title
}

export class GetUserTxListByDIDRoleResponseItem1 extends GetUserTxListByDIDRoleResponseItem {
  didContent:string = ""; // did摘要内容
  groupContent:string = ""; // 群title
}
export class GetUserTxListByDIDRoleResponse {
  public items:ClassArray<GetUserTxListByDIDRoleResponseItem> = new ClassArray<GetUserTxListByDIDRoleResponseItem>(GetUserTxListByDIDRoleResponseItem)
}

export class GetUserTxListByDIDRoleResponse1 {
  public items:ClassArray<GetUserTxListByDIDRoleResponseItem1> = new ClassArray<GetUserTxListByDIDRoleResponseItem1>(GetUserTxListByDIDRoleResponseItem1)
}

export class GetUserTxListByDIDRoleRequest {
  public address:string = "";
  public role:DIDRole = DIDRole.Creator

  public startTxTime:number = 0;

  public size:number = 10;
}

export async function GetUserTxListByDIDRole(request:GetUserTxListByDIDRoleRequest):Promise<[GetUserTxListByDIDRoleResponseItem1[], Error|null]> {
  const [ret, err] =  await Send("/browser/GetUserTxListByDIDRole", request, GetUserTxListByDIDRoleResponse);
  if (err) {
    return [[], err]
  }
  const groupAddressList = Array.from(new Set(ret.items.map(it => it.transferChat).filter(it => it !== '-')));
  const didAddressList = Array.from(new Set(ret.items.map(it => it.didAddress).filter(it => it !== '-')));

  const retItems = ret.items.map(it => {
    return {...it, groupContent: '', didContent: '' }
  });
  const [groupBriefInfo, didArticleBriefInfo] = await Promise.allSettled([
    groupAddressList.length > 0 ? GetGroupBriefInfo(groupAddressList) : [],
    didAddressList.length > 0 ? GetDIDArticleBriefInfo(didAddressList) : []
  ])
  if (groupAddressList.length && groupBriefInfo.status === "fulfilled") {
    for(let i = 0; i < groupBriefInfo.value.length; i++) {
      const item = groupBriefInfo.value[i]
      retItems.forEach(it => {
        if (it.transferChat === item.address) {
          it.groupContent = item.name
        }
      })
    }
  }
  if (didAddressList.length && didArticleBriefInfo.status === "fulfilled") {
    for(let i = 0; i < didArticleBriefInfo.value.length; i++) {
      const item = didArticleBriefInfo.value[i]
      retItems.forEach(it => {
        if (it.didAddress === item.address) {
          it.didContent = item.crMetaText
        }
      })
    }
  }
  return [retItems, err]
}
