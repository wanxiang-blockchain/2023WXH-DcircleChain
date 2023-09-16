import {PostJsonNoToken} from "../3rdparty/ts-baselib";
import {getUs} from "../DIDBrowser";

export class BucketInfo {
  endpoint:string = ""
  bucketName:string = ""
}

class Data {
  bucketInfo:BucketInfo = new BucketInfo();
}

export class GetBucketInfoResponse {
  public data: Data = new Data();
}

export async function GetBucketInfo():Promise<[BucketInfo, Error | null]> {
  const net = getUs().nf.get();
  const [ret, err] = await PostJsonNoToken("/browser/GetBucketInfo",{data: {}}, GetBucketInfoResponse, net)
  if (err) {
    return [new BucketInfo(), err];
  }
  return [ret.data.bucketInfo, null]
}
