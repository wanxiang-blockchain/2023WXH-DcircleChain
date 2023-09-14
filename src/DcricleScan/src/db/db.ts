import Dexie, { Table } from 'dexie';
import {DBFile} from "./DBFile";
import {DBMessage} from "./DBMessage";
import {Env} from "../config";
import {DBBucket} from "./DBBucket";
import {DBDIDArticleStat} from "./DBDIDArticleStat";
import {DBGroup} from "./DBGroup";
import {DBDIDArticleRole} from "./DBDIDArticleRole";

export class DcircleScan extends Dexie {
  file!: Table<DBFile.Document>
  message!: Table<DBMessage.Document>
  bucket!: Table<DBBucket.Document>
  didArticleStat!:Table<DBDIDArticleStat.Document>
  group!: Table<DBGroup.Document>
  didArticleRole!: Table<DBDIDArticleRole.Document>

  constructor() {
    super(`dcirclescan_${Env}.db`);
    this.version(2).stores({
      file: `&${DBFile.Fields.id}`,
      message: `&${DBMessage.Fields.msgId}, ${DBMessage.Fields.realChatId}`,
      bucket:`&${DBBucket.Fields.id}`,
      didArticleStat:`&${DBDIDArticleStat.Fields.statId}`,
      group: `&${DBGroup.Fields.id}`,
      didArticleRole: `&${DBDIDArticleRole.Field.id}`
    })
  }
}

export function GetDScan() {
  return new DcircleScan();
}
