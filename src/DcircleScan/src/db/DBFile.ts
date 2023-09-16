import {DcircleScan} from "./db";

export namespace DBFile {
  export enum Fields {
    id = "id",
    File = "file",
  }

  export interface Document {
    id:string;
    file:Uint8Array;
  }


  export async function Write(selfDB:DcircleScan, id:string, file:Uint8Array):Promise<Error|null> {
    try {
      await selfDB.file.put({id:id, file:file});
      return null;
    } catch (e) {
      return new Error((e as Error).message??"fail")
    }
  }

  export async function Read(selfDB:DcircleScan, id:string):Promise<[Uint8Array, Error|null]> {
    const docs = await selfDB.file.where(Fields.id).equals(id).toArray();
    if (docs.length<=0) {
      return [undefined!, new Error("not found")];
    }

    if (docs.length>1) {
      throw new Error(`FindById(${id}) docs's length larger than 1`);
    }

    return [docs[0].file, null];
  }

  export async function Has(selfDB:DcircleScan, id:string):Promise<boolean> {
    const docs = await selfDB.file.where(Fields.id).equals(id).toArray();
    return docs.length>0;
  }
}
