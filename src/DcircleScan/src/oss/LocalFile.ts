import {DBFile} from "../db/DBFile";
import {GetDScan} from "../db/db";

export interface LocalFile {
  write(objectId:string, data:Uint8Array):Promise<Error|null>;
  read(objectId:string):Promise<[Uint8Array, Error|null]>;
  has(objectId:string):Promise<boolean>;
  path(objectId:string):string;
}

class DBLocalFile implements LocalFile {
  async has(objectId: string): Promise<boolean> {
    return await DBFile.Has(GetDScan(), this.path(objectId));
  }

  async read(objectId: string): Promise<[Uint8Array, (Error | null)]> {
    return await DBFile.Read(await GetDScan(), this.path(objectId))
  }

  async write(objectId: string, data: Uint8Array): Promise<Error | null> {
    return await DBFile.Write(await GetDScan(), this.path(objectId), data);
  }

  path(objectId: string): string {
    return objectId;
  }
}

export function getLocalFile():LocalFile {
  return new DBLocalFile();
}
