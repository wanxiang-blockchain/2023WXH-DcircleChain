import {Item, Table} from "./table"
import {UserSpace} from "../userspace"


class TokenItem implements Item{
  id: string
  token: string

  constructor(name: string, token: string) {
    this.id = name
    this.token = token
  }
}

export function TokenTable(us: UserSpace): Token {
  return us.selfDB.table("token", TokenItem, Token)
}

export class Token extends Table<TokenItem>{
  static readonly Empty = ""

  async value(name: string): Promise<string> {
    let v = await this.get(name)
    if (v === undefined) {
      return Token.Empty
    }

    return v.token
  }

  async setValue(name: string, v:string) {
    this.updateOrInsert(name, new TokenItem(name, v))
  }

  async clear(name:string) {
    this.delete(name)
  }
}

