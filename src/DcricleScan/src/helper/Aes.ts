import {SignNonceStr} from "../3rdparty/ts-baselib/src/api/api";
import {toHex, toUint8Array} from "./Hex";
import {aes_decrypt, aes_encrypt} from "rustlib_crypto";

export class Aes {
  constructor(public readonly key:string) {

  }

  public toString():string {
    return JSON.stringify(this);
}

  decrypt(data:Uint8Array):Uint8Array {
    return aes_decrypt(data, this.key, '')
  }

  encrypt(data:Uint8Array):Uint8Array {
    if (!(this.key.length === 32)) {
      throw new Error(`Aes key must 32 length hex string`)
    }
    return aes_encrypt(data, this.key, '')
  }
}

export function newAesKey():string {
  return toHex(toUint8Array(SignNonceStr(16)))
}

export function newAesIv():string {
  return toHex(toUint8Array(SignNonceStr(16)))
}
