import { NcEvent } from '../3rdparty/ts-nc';
import {DcircleScan} from "./db";

export namespace DBDIDArticleStat {
  export class ChangedEvent extends NcEvent<string> {
    static sym = Symbol();
  }

  export enum StatRoleType {
    articleStat = 'ArticleStat',
    creatorStat = 'CreatorStat',
    transferStat = 'TransferStat',
    groupStat = 'GroupStat',
    singleGroupStat = 'SingleGroupStat', // 单个群聊，id则为chatId
    consumerStat = 'ConsumerStat',
    unknow = ''
  }

  export enum Fields {
    id = 'id',
    statId = 'statId'
  }

  export interface Document {
    statId:string;
    dataUpdateTime:number;
    roleId:string;
    roleStatType: StatRoleType;
    ymd: string;
    contentNums:number;
    circulationNums:number;
    consumerNums:number;
    consumptionTimes:number;
    reachNums:number;
    groupNums:number;
    potentialCirculationNums:number;
    potentialConsumerNums:number;
    sgNums:number;
    tTimes:number;
    exposureGroupNums:number;
    exposurePeopleNums:number;
    revenueNums:number;
  }

  export async function InsertOrUpdate(selfDB: DcircleScan, ...docs: Document[]): Promise<Error | null> {
    try {
      const ret = await selfDB.didArticleStat.bulkPut(docs);
    } catch (e) {
      console.warn('Insert catch e', e);
    }
    return null;
  }

  export async function FindById(selfDB: DcircleScan, id: string): Promise<[Document, Error | null]> {
    try {
      const first = await selfDB.didArticleStat.get(id);
      return first ? [first, null] : [{} as Document, new Error('not found')];
    } catch (e) {
      console.warn('FindById catch e ', e);
      return [{} as Document, new Error('FindById fail')];
    }
  }

  export async function FindLatestByRole(selfDB: DcircleScan, roleId:string, userRoles: StatRoleType[]):Promise<Document[]> {
    const promises = [];
    for(let i = 0; i < userRoles.length; i++) {
      const role = userRoles[i];
      promises.push(findLatestByRole(selfDB, roleId, role))
    }
    const result = await Promise.allSettled(promises);
    const docs = [];
    for(let i = 0; i < result.length; i++) {
      const ret = result[i];
      if (ret.status == 'fulfilled' && ret.value != null) {
        docs.push(ret.value)
      }
    }
    return docs;
  }

  async function findLatestByRole(selfDB: DcircleScan, address:string, role: StatRoleType):Promise<Document | null> {
    const doc = await selfDB.didArticleStat.filter(doc => doc.roleId === address && doc.roleStatType === role).first();
    if (!doc) return null;
    return doc;
  }

  export async function FindLatestByArticle(selfDB: DcircleScan, didArticleAddress:string):Promise<[Document, Error | null]> {
    const doc = await findLatestByRole(selfDB, didArticleAddress, StatRoleType.articleStat)
    if (!doc) return [{} as Document, new Error('not find')]
    return [doc, null];
  }

  export async function FindByIds(selfDB: DcircleScan, ids: string[]): Promise<[Document[], Error | null]> {
    try {
      if (ids.length === 0) {
        return [[], null];
      }
      let docs = await selfDB.didArticleStat.where(Fields.id).anyOf(ids).toArray();
      return [docs, null];
    } catch (e) {
      console.warn('FindByIds catch e ', e);
      return [[], new Error('FindByIds fail')];
    }
  }
}
