import {DB} from "./db"
import {AsyncLocker} from "../asynclocker"

export interface Item {
  id: string
}


class Num {
  constructor(public no:number) {
  }
}

class Data<T extends Item> extends Num{
  constructor(no:number, public d:T) {
    super(no)
  }
}

/**
 * xxx.id-${id} : Data
 * xxx.meta.0: Meta0
 * xxx.meta.${no}: IDs
 *
 */

class IDs {
  constructor(public ids:string[]) {
  }
}

class Meta0 {
  constructor(public maxNo: number) {
  }
}

export class TableFactory {

  private tables = new Map<string, Table<Item>>()

  constructor(private readonly db: DB) {
  }

  public get<E extends Item, T extends Table<E>>(
    name:string, itemConstructor: {new (...args:any[]):E}
    ,clazz: TableConstructor<E, T>): T {

    // todo:  clazz (default) = Table

    let old = this.tables.get(name)
    if (old !== undefined) {
      const err = old.checkItemCon(itemConstructor)
      if (err !== null) {
        throw err
      }
      return old as T
    }

    let n = new clazz(name, itemConstructor, this.db) as Table<Item>
    this.tables.set(name, n)
    return n as T
  }
}

const MAX_PER_ARR = 100

export interface TableConstructor<E extends Item, T extends Table<E>> {
  new (name:string, itemConstructor: {new (...args:any[]):E}, db: DB):T
}

export class Table<T extends Item> {
  private locker = new AsyncLocker()

  constructor(private readonly name:string, private itemConstructor: {new (...args:any[]):T}
    , private readonly db: DB) {
  }

  public checkItemCon(itemConstructor: {new (...args:any[]):T}):Error|null {
    if (this.itemConstructor !== itemConstructor) {
      return Error(`table<${this.name}> has different item`)
    }

    return null
  }

  private newData():Data<T> {
    return new Data<T>(0, new this.itemConstructor())
  }

  // return: start
  private async metaNum(step: number = 1):Promise<number> {
    if (step <= 0) {
      throw Error("step must be > 0")
    }

    const key0 = Table.metaKey(0)
    await this.locker.lock(key0)
    let meta0 = await this.db.get(this.getName(key0), Meta0) || new Meta0(0)
    let start = meta0.maxNo + 1
    meta0.maxNo += step
    await this.db.set(this.getName(key0), meta0)
    this.locker.unlock(key0)
    return start
  }

  private async saveMeta(id:string, num: number) {
    const no = Math.ceil(num/MAX_PER_ARR)
    const key = Table.metaKey(no)
    await this.locker.lock(key)
    let old = await this.db.get(this.getName(key), IDs) || new IDs([])
    old.ids.push(id)
    await this.db.set(this.getName(key), old)
    await this.locker.unlock(key)
  }

  async get(id:string): Promise<T|undefined> {
    let old = await this.db.get(this.getName(Table.idKey(id)), this.newData())
    if (old === undefined) {
      return undefined
    }
    (old.d.id as string)= id
    return old.d as T
  }

  async getIds(): Promise<string[]> {
    const key0 = Table.metaKey(0)
    let ret:string[] = []
    let meta0 = await this.db.get(this.getName(key0), Meta0) || new Meta0(0)
    for (let i = 1; i < meta0.maxNo; i = i+MAX_PER_ARR) {
      let no = Math.ceil(i/MAX_PER_ARR)
      let key = Table.metaKey(no)
      let old = await this.db.get(this.getName(key), IDs) || new IDs([])
      ret = ret.concat(old.ids as string[])
    }

    let s = new Set<string>()
    for (let id of ret) {
      if (await this.db.has(this.getName(Table.idKey(id)))) {
        s.add(id)
      }
    }

    return Array.from(new Set(s))
  }

  // todo: batch insert

  async insert(id:string, item: T) {
    item.id = id
    const key = Table.idKey(id)
    await this.locker.lock(key)
    let num = await this.metaNum()
    await this.saveMeta(id, num)
    await this.db.set(this.getName(key), new Data(num, item))
    this.locker.unlock(key)
  }

  async delete(id: string) {
    // todo: delete meta
    const key = Table.idKey(id)
    await this.locker.lock(key)
    await this.db.remove(this.getName(key))
    this.locker.unlock(key)
  }

  async delAll() {
    const key0 = Table.metaKey(0)
    await this.locker.lock(key0)
    let meta0 = await this.db.get(this.getName(key0), Meta0)
    if (meta0 === undefined) {
      this.locker.unlock(key0)
      return
    }

    let ids = await this.getIds()
    for (let id of ids) {
      await this.delete(id)
    }

    for (let i = 1; i < meta0.maxNo; i = i+MAX_PER_ARR) {
      let no = Math.ceil(i/MAX_PER_ARR)
      let key = Table.metaKey(no)
      await this.db.remove(this.getName(key))
    }

    await this.db.remove(this.getName(key0))

    this.locker.unlock(key0)
  }

  async updateOrInsert(id:string, data: Partial<T>) {
    let idKey = Table.idKey(id)
    await this.locker.lock(idKey)
    let old = await this.db.get(this.getName(idKey), this.newData())
    let num = old?.no
    if (num === undefined) {
      num = await this.metaNum()
      await this.saveMeta(id, num)
    }

    let nItem = {...old?.d as T, ...data, ...{id: id}}
    await this.db.set(this.getName(idKey), new Data(num, nItem))
    this.locker.unlock(idKey)
  }

  private static idKey(id:string):string {
    return "id-" + id
  }

  private static metaKey(no:number):string {
    return "meta-" + no
  }

  private getName(key:string):string {
    return this.name + "." + key
  }
}

