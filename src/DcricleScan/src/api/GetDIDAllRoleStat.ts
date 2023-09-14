import {Send} from "./api";

export class StatDailyResponseItem {
  id: string = "";
  dataUpdateTime: number = 0;
  roleStatType: string = "";
  ymd: string = "";
  nNums: number = 0;
  mNums: number = 0;
  cNums: number = 0;
  cTimes: number = 0;
  tNums: number = 0;
  tTimes: number = 0;
  gNums: number = 0;
  joinTimes: number = 0;
  joinUserCount: number = 0;
  shareGroupCount: number = 0;
  mxNums: number = 0;
  cxNums: number = 0;
  sgNums: number = 0;
  exposureGroupNums: number = 0;
  exposurePeopleNums: number = 0;
  revenueNums: number = 0;
}
export class GetDIDAllRoleStatRequest {
  userAddress: string = "";
}
export class GetDIDAllRoleStatRequestResponse {
  items: StatDailyResponseItem[] = [];
}
// 获取个人全部角色统计
export async function GetDIDAllRoleStat(didAddress: string): Promise<[GetDIDAllRoleStatRequestResponse, (Error | null)]> {
  const request = new GetDIDAllRoleStatRequest();
  request.userAddress = didAddress;
  const [ret, err] = await Send(
      "/browser/GetDIDAllRoleStat",
      request,
      GetDIDAllRoleStatRequestResponse
  );
  return [ret, err];
}
