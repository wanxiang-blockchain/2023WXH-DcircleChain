import {Send} from "./api";
import {DIDRole} from "./DIDRole";
import {DBDIDArticleRole} from "../db/DBDIDArticleRole";
import {GetDScan} from "../db/db";
import {getUs} from "../DIDBrowser";

export class GetUserStatByDIDRoleResponse implements DBDIDArticleRole.Document {
  id:string = "";
  address:string = ""
  dataUpdateTime: number=0;
  nNums: number=0; // 内容总数 交易篇数 传播篇数
  mNums: number=0; //  流通量
  cNums: number=0; // 消费人数
  cTimes: number=0; // 消费次数 交易次数
  tTimes: number = 0; // 消费者购买后只统计该消费者传播次数
  tNums: number=0; // 传播人数
  gNums: number=0; // 传播群数 交易群数
  mxNums: number=0; // 潜力流通量
  cxNums: number=0; // 潜力消费人数
  sgNums:number=0; // 只统计消费者在【公开群/频道】消费过的数量
  revenueNum0s:number=0; // 个人收入
}

export class GetUserStatByDIDRoleRequest {
  public address:string = "";
  public role:DIDRole = DIDRole.Creator
}

export async function GetUserStatByDIDRole(request:GetUserStatByDIDRoleRequest,source: string = ''):Promise<null|Error> {
  const [ret, err] = await Send("/browser/GetUserStatByDIDRole", request, GetUserStatByDIDRoleResponse);
  if (err) {
    return err;
  }
  ret.id = `${ret.address}_${request.role}`
  await DBDIDArticleRole.InsertOrUpdate(GetDScan(), ret);
  await getUs().nc.post(DBDIDArticleRole.ChangedEvent.New([request.address], source))
  return null;
}
