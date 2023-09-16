
import {HttpBuilder} from "./http/http"
import {StreamBuilderCreator} from "./http/stream"
import {Token} from "../db/token"



export class NetFactory {
  static readonly default = new NetFactory()

  private nets:Map<string, Net> = new Map<string, Net>()

  get(name:string, token: Token): Net{
    let old = this.nets.get(name)
    if (old === undefined) {
      old = new Net(name, token)
      this.nets.set(name, old)
    }

    return old
  }
}

export class Net {
  public constructor(public readonly name:string, private readonly token: Token) {
  }

  /**
   * 401 表示net层不通，连通net层的方法常常是 login
   */

  // 连续的多次401，只处理一次
  process401():void {
    if (this.has401_) {
      return
    }
    this.has401_ = true
    this.net401Delegate_(this)
  }

  set401Delegate(net401Delegate: ((net: Net) => void)): void {
    this.net401Delegate_ = net401Delegate;
  }

  is401(): boolean {
    return this.has401_
  }

  // 通常清除401的场景：1、任何需要token的接口，非401返回时；2、登录成功时(给net设置有效的token值)；3、业务其他逻辑主动清除
  public clear401(): void {
    this.has401_ = false
  }

  public setBaseUrl(urls: string) {
    this.baseUrl_ = urls
  }

  public getBaseUrl(): string {
    return this.baseUrl_
  }

  // 每次获取builder时，在net层都应该调用生成函数生成一个builder 而不能在net层直接缓存一个builder直接返回
  // 对于http等无状态的请求，每次都应该是一个全新的builder 而对于其他类型的协议，有可能不是每次都是全新的一个builder
  // 具体如何生成，应该是creator的责任，而不是net来负责，net负责的是会话级别的连接，builder负责的是具体的协议
  public setHttpBuilderCreator(creator: (baseUrl: string) => HttpBuilder): void {
    this.creator_ = creator
  }

  getHttpBuilder(): HttpBuilder {
    return this.creator_(this.getBaseUrl())
  }

  async setToken(token: string) {
    if (token !== Token.Empty) {
      this.clear401()
    }
    this.token_ = token
    await this.token.setValue(this.name, token)
  }

  async getToken(): Promise<string> {
    if (this.token_ === Token.Empty) {
      this.token_ = await this.token.value(this.name)
    }

    return this.token_
  }

  async clearToken() {
    this.token_ = Token.Empty
    await this.token.clear(this.name)
  }

  private token_: string = Token.Empty
  private has401_: boolean = false
  private creator_: (baseUrl: string) => HttpBuilder = StreamBuilderCreator()
  private baseUrl_: string = ""
  private net401Delegate_: ((net: Net) => void) = ((_net: Net) => {
  })
}
