import {NcEvent} from "../3rdparty/ts-nc";
import {DcircleScan} from "./db";

export namespace DBBucket {
  export class UploadProgressEvent extends NcEvent<string>{
    static sym = Symbol()
  }

  export class UploadFailEvent extends NcEvent<string> {
    static sym = Symbol()
  }

  export class DownloadProgressEvent extends NcEvent<string> {
    static sym = Symbol()
  }

  export class DownloadFailEvent extends NcEvent<string> {
    static sym = Symbol()
  }

  export enum Fields {
    id = "id",
    progress = "progress",
    checkpoint = "checkpoint",
  }

  export interface Document {
    id:string;
    progress:number;
    checkpoint?:string;
  }

  export async function GetList(selfDB:DcircleScan) {
    return selfDB.bucket.toArray();
  }

  export async function Insert(selfDB:DcircleScan, doc:Document):Promise<Error|null> {
    try {
      await selfDB.bucket.add(doc);
      return null;
    } catch (e) {
      return new Error((e as Error).message??"exe fail");
    }
  }

  export async function SetProgress(selfDB:DcircleScan,id:string, progress:number, checkpoint?:string):Promise<Error|null> {
    try {
      const ret = await selfDB.bucket.where(Fields.id).equals(id).modify({ progress: progress, checkpoint:checkpoint});
      return ret ? null : new Error(`can not find id:${id}`);
    } catch (e) {
      console.warn(`SetProgress(id:${id}, progress:${progress}) err`, e);
      return new Error((e as Error).message ?? 'unknown error');
    }
  }

  export async function FindById(selfDB:DcircleScan, id:string):Promise<[Document, Error|null]> {
    const doc = await selfDB.bucket.where(Fields.id).equals(id).first()
    return doc?[doc, null]:[{} as Document, new Error("not found")];
  }

  export async function GetProgress(selfDB:DcircleScan, ids:string[]):Promise<number> {
    ids = Array.from(new Set(ids));
    if (ids.length<=0) {
      throw new Error("ids's length must larger than 0.")
    }

    const docs = await selfDB.bucket.where(Fields.id).anyOf(ids).toArray();
    if (docs.length !== ids.length) {
      console.error(`ids's length not eq docs`, ids, docs)
      throw new Error("ids's length not eq docs")
    }
    let progress = 0;
    for (let doc of docs) {
      progress += doc.progress;
    }

    return progress/ids.length
  }
}
