import {decodeBase64} from "../../helper/getBase64";
import {toHex} from "../../helper/Hex";
import {NavigateFunction} from "react-router-dom";
import {getHttp} from "../../helper/handleUrl";
import {GetShortLinkInfoByID, GetShortLinkInfoByIDRequest, Context} from "../../api/GetShortLinkInfoByID";
import {GetInviteCodeInfo, GetInviteCodeInfoRequest} from "../../api/GetInviteCodeInfo";

export enum AddressType {
  default = 'default', // 如果没找到配置的地址，则跳转到下载页面
  'c' = 'c', // did内容 article
  'l' = 'l', // 短链
  'g' = 'g', // 群门户
}

export interface BuildAddress {
  getAddress(data: string): string | Promise<string>;
  redirectTo(navigate: NavigateFunction, address: string): void;
}


export class AddressFactory {
  static build(type: AddressType) {
    const AddressTypeMap:Map<AddressType, () => BuildAddress> = new Map<AddressType, () => BuildAddress>([
      [AddressType.c, () => new DiDContent()],
      [AddressType.l, () => new LinkContent()],
      [AddressType.g, () => new GroupContent()],
      [AddressType.default, () => new DefaultContent()],
    ])
    if (!AddressTypeMap.has(type)) {
      return AddressTypeMap.get(AddressType.default)!();
    }
    return AddressTypeMap.get(type)!();
  }
}

export class GroupContent implements BuildAddress {
  public url:string = "";
  async getAddress(data:string):Promise<string> {
    const buffer: Uint8Array = decodeBase64(data);
    const shortIdBytes = buffer.slice(1, buffer.length - 1);
    const byteArray: number[] = Array.from(shortIdBytes);
    const id = String.fromCharCode(...byteArray);
    const request = new GetInviteCodeInfoRequest();
    request.inviteCode = id;
    const [ret, err] = await GetInviteCodeInfo(request);
    if (err) {
      return '';
    }
    return ret.groupId;
  }
  redirectTo(route:NavigateFunction, uid:string) {
    if (!uid) return;
    route(`/group/${uid}?link=${encodeURIComponent(getHttp())}`, {replace: true, state: {url: getHttp()}})
  }
}


export class DiDContent implements BuildAddress {
  public url:string = "";
  getAddress(data:string):string {
    const buffer = decodeBase64(data);
    const address = buffer.slice(1 + 4, 1 + 4 + 20);
    return `0x${toHex(address)}`
  }
  redirectTo(route:NavigateFunction, uid:string) {
    if (!uid) return;
    route(`/article/${uid}?link=${encodeURIComponent(getHttp())}`, {replace: true, state: {url: getHttp()}})
  }
}

export enum IdType {
  InviteDownLoad = 'InviteDownLoad',
  GroupInviteDownLoad = 'GroupInviteDownLoad'
}

export class LinkContent implements BuildAddress {
  public type:IdType = IdType.InviteDownLoad
  async getAddress(data: string): Promise<string> {
    const buffer: Uint8Array = decodeBase64(data);
    const shortIdBytes = buffer.slice(1, buffer.length - 1);
    const byteArray: number[] = Array.from(shortIdBytes);
    const request = new GetShortLinkInfoByIDRequest();
    request.shortId = String.fromCharCode(...byteArray);
    const [ret, err] = await GetShortLinkInfoByID(request);
    const context: Context = JSON.parse(ret.context);
    this.type = context.idType
    if (context.idType == IdType.InviteDownLoad) {
      return context.didAddress;
    }
    if (context.idType == IdType.GroupInviteDownLoad) {
      return context.groupId;
    }
    return '';
  }

  redirectTo(route:NavigateFunction, uid:string) {
    if (!uid) return;
    const map = new Map([
      [IdType.GroupInviteDownLoad, 'group'],
      [IdType.InviteDownLoad, 'article'],
    ])
    route(`/${map.get(this.type)}/${uid}?link=${encodeURIComponent(getHttp())}`, {replace: true, state: {url: getHttp()}})
  }
}

export class DefaultContent implements BuildAddress {
  async getAddress():Promise<string> {
    return "";
  }
  redirectTo(route:NavigateFunction) {
    route(`/download?link=${encodeURIComponent(getHttp())}`, {replace: true})
  }

}
