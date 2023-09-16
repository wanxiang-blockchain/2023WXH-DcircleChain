import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";

class DIDBriefInfoItem {
  address:string = ''
  crMetaText:string = ''
}
class GetDIDBriefInfoResponse{
  public items:ClassArray<DIDBriefInfoItem> = new ClassArray<DIDBriefInfoItem>(DIDBriefInfoItem)
}

// addressList最多100长度
export async function GetDIDArticleBriefInfo(addressList: string[]):Promise<DIDBriefInfoItem[]> {
  const [ret, err] = await Send("/browser/GetDIDBriefInfo", {addressList: addressList}, GetDIDBriefInfoResponse);
  if (err) {
    return []
  }
  return ret.items

}
