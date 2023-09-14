import {Item, Table, TableConstructor, TableFactory} from "./db/table"
import {DB as DBInner} from "./db/db"
import { NC } from "../../ts-nc"
import {Net, NetFactory} from "./api/net"
import {BlStorage} from "./db/storage"
import {TokenTable} from "./db/token"

export interface DBInterface {
  table<E extends Item, T extends Table<E>>(
    name: string, itemConstructor: {new (...args:any[]):E}, clazz?: TableConstructor<E, T>): T
}

export interface UserSpace {
  readonly selfDB: DBInterface
  readonly shareDB: DBInterface
  readonly nc: NC
  readonly nf: { get(name?: string): Net }

  clone(uid: string, becauseOfNet?: string): Promise<UserSpace>

  getUid():string;
}

export type ReUserSpace = {- readonly [P in keyof UserSpace]: UserSpace[P]}

export class DB implements DBInterface{
  private readonly tf: TableFactory

  constructor(name:string, blStorage: BlStorage) {
    this.tf = new TableFactory(new DBInner(name, blStorage))
  }

  table<E extends Item, T extends Table<E>>(name: string
    , itemConstructor: { new(...args: any[]): E }, clazz: TableConstructor<E, T>): T {

    return this.tf.get(name, itemConstructor, clazz)
  }

}

class dummyStorage implements BlStorage{
  async get<T>(_key: string, _con: { new(...args: any[]): T }): Promise<T | undefined> {
    return undefined;
  }

  async has(_key: string): Promise<boolean> {
    return false;
  }

  async remove(_key: string): Promise<void> {
  }

  async set<T>(_key: string, _value: T): Promise<void> {
  }

}

class Me implements Item{
  id:string = "me"
  uid:string = ""
}

export class AloneUserSpaceSync implements ReUserSpace{
  nc: NC
  nf: { get(name?: string): Net }
  shareDB: DB

  selfDB: DB

  private uid: string = ""

  public getUid():string {
    return this.uid
  }

  async clone(uid: string, _becauseOfNet?: string): Promise<UserSpace> {
    if (uid === this.uid) {
      return this
    }

    let ret = new AloneUserSpaceSync(this.blStorage, this.baseUrl)
    ret.init(uid)
    return ret
  }

  init(uid:string) {
    this.uid = uid
    this.selfDB = new DB(`u_${uid}`, this.blStorage)
  }

  constructor(public readonly blStorage:BlStorage, public readonly baseUrl: string) {
    this.shareDB = new DB("share", blStorage)

    this.nc = new NC("aloneUserSpace")
    this.selfDB = new DB("", new dummyStorage())

    let us = this
    this.nf = new class {
      readonly nf = new NetFactory()
      get(name: string = "main"): Net {
        let ret = this.nf.get(name, TokenTable(us))
        ret.setBaseUrl(baseUrl)
        return ret
      }
    }
  }
}

export class AloneUserSpace extends AloneUserSpaceSync{

  async clone(uid: string, _becauseOfNet?: string): Promise<UserSpace> {

    if (uid === this.getUid()) {
      return this
    }

    await this.shareDB.table("me", Me, Table).updateOrInsert("me", {uid: uid})
    let ret = new AloneUserSpace(this.blStorage, this.baseUrl)
    await ret.init()
    return ret
  }

  async init() {
    const uid = (await this.shareDB.table("me", Me, Table).get("me"))?.uid
    if (uid === undefined || uid === "") {
      return
    }

    super.init(uid)
  }
}
