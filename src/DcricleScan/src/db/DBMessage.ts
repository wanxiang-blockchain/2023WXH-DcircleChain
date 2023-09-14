import {DcircleScan} from "./db";
import {Encrypted} from "../helper/EncryptorBuilderFactory";
import {Type} from "../helper/Message";
import {NcEvent} from "../3rdparty/ts-nc";

export namespace DBMessage {

  export class ChangedEvent extends NcEvent<string> {
    static sym = Symbol();
  }
  export enum Fields {
    msgId = 'msgId',
    realChatId = "realChatId"
  }

  export interface Document {
    msgId:string;
    chatId:string;
    realChatId:string;
    seq:number;
    content:string;
    encrypted:Encrypted;
    creatorUid:string;
    createTime:number;
    msgType:Type;
  }

  export async function Insert(selfDB: DcircleScan, ...docs: Document[]):Promise<null> {
    try {
      const ret = await selfDB.message.bulkPut(docs)
      return null
    } catch(e) {
      console.log(e)
      return null
    }
  }

  export async function PullDownMessage(selfDB: DcircleScan, address: string, end:number, limit:number = 10):Promise<Document[]> {
    try {
      let docs = await selfDB.message.where({realChatId: address}).filter(doc => {
        return doc.seq > end
      }).toArray();
      docs = docs.slice(0, Math.min(limit, docs.length));
      docs = docs.sort((a, b) => b.seq - a.seq)
      return docs;
    } catch(e) {
      console.log(e, '获取出现错误')
      return [];
    }
  }
}
