import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

class GroupBriefInfoItem {
  address:string = ''
  name:string = ''
}
class GetGroupBriefInfoResponse{
  public items:ClassArray<GroupBriefInfoItem> = new ClassArray<GroupBriefInfoItem>(GroupBriefInfoItem)
}

// addressList最多100长度
export async function GetGroupBriefInfo(addressList: string[]):Promise<GroupBriefInfoItem[]> {
  const [ret, err] = await Send("/browser/GetGroupBriefInfo", {addressList: addressList}, GetGroupBriefInfoResponse);
  if (err) {
    return []
  }
  return ret.items

}
