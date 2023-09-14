import {DcircleScan} from "./db";
import {NcEvent} from "../3rdparty/ts-nc";

export namespace DBDIDArticleRole {
  export enum Field {
    address = "address",
    id = 'id'
  }
  export interface Document {
    id:string;
    address:string;
    dataUpdateTime: number;
    nNums: number;
    mNums: number;
    cNums: number;
    cTimes: number;
    tTimes: number;
    tNums: number;
    gNums: number;
    mxNums: number;
    cxNums: number;
    sgNums:number;
    revenueNum0s:number;
  }

  export class ChangedEvent extends NcEvent<string> {
    static sym = Symbol();
    source:string = "";
    public static New(adds:string[], source:string):ChangedEvent {
      const event = new ChangedEvent(adds);
      event.source = source;
      return event
    }
  }

  export async function InsertOrUpdate(selfDB: DcircleScan, ...docs: Document[]) {
    try {
      await selfDB.didArticleRole.bulkPut(docs)
    } catch (e) {
      console.warn('DBDIDArticleRole Insert catch e', e);
    }
  }

  export function buildId(address:string, type:string) {
    return `${address}_${type}`
  }

  export async function FindById(selfDB: DcircleScan, id:string):Promise<[Document, Error|null]> {
    try {
      const doc = await selfDB.didArticleRole.where({id: id}).first();
      if (!doc) {
        return [{} as Document, new Error('not found')]
      }
      return [doc, null]
    } catch (err) {
      return [{} as Document, new Error('not found')]
    }

  }
}
