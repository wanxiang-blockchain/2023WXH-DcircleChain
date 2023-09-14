

export interface BlStorage {
  set<T>(key:string, value:T):Promise<void>
  get<T>(key:string, con:{new (...args:any[]):T}|T):Promise<T|undefined>
  has(key:string): Promise<boolean>
  remove(key:string): Promise<void>
}

export class MemoryStorage implements BlStorage{
  private readonly storage = new Map<string, any>()

  async get<T extends {[P in keyof T]:T[P]}>(key:string, _con:{new (...args:any[]):T}|T)
      :Promise<T|undefined> {
    return this.storage.get(key);
  }

  async remove(key: string): Promise<void> {
    this.storage.delete(key);
  }

  async set<T>(key:string, value:T): Promise<void> {
    this.storage.set(key, value);
  }

  async has(key:string):Promise<boolean> {
    return this.storage.has(key)
  }
}

export class LocalStorage implements BlStorage{
  async get<T extends {[P in keyof T]:T[P]}>(key:string, _con:{new (...args:any[]):T}|T)
      :Promise<T|undefined> {
    const value = window.localStorage.getItem(key)
    if (!value) {
      return undefined
    }

    try {
      return JSON.parse(value)
    } catch (e) {
      return undefined
    }
  }

  async remove(key: string): Promise<void> {
    window.localStorage.removeItem(key)
  }

  async set<T>(key:string, value:T): Promise<void> {
    window.localStorage.setItem(key, JSON.stringify(value))
  }

  async has(key:string):Promise<boolean> {
    return !!window.localStorage.getItem(key)
  }
}
