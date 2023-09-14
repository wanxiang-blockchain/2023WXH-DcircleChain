import {Aes} from "./Aes";
import {Json} from "../3rdparty/ts-json";
import {fromHex, fromUint8Array, toHex, toUint8Array} from "./Hex";
import {message_decode, message_encode} from "rustlib_crypto";
import {Env} from "../config";

export interface Encryptor {
  setKey(aes: Aes): Encryptor;

  encode<T extends object | string>(content: T): string;

  decode<T extends object | string>(
    content: string,
    prototype: { new(...args: any[]): T } | T
  ): [T, Error | null];
}
interface EncryptorBuilder {
  build(encrypted: Encrypted): Encryptor;
}

export enum Encrypted {
  Json = 0,
  AES = 1,
}

export class JsonEncryptor implements Encryptor {
  setKey(key: Aes): Encryptor {
    return this;
  }

  decode<T extends object | string>(
    content: string,
    prototype: { new(...args: any[]): T } | T
  ): [T, Error | null] {
    if (prototype.constructor.name.toLowerCase() === "string") {
      return [content as T, null];
    }

    try {
      return new Json().fromJson<T>(content, prototype);
    } catch (e) {
      console.error("decode", content, e);
      throw e;
    }
  }

  encode<T extends object | string>(content: T): string {
    if (typeof content === "string") {
      return content;
    }

    return new Json().toJson(content);
  }
}


export class MessageCoder extends JsonEncryptor implements Encryptor {
  private version:number = 1
  private aes: Aes = new Aes("");

  setKey(aes: Aes): Encryptor {
    this.aes = aes;
    return this;
  }

  encode<T extends object | string>(content:T):string {
    const json = super.encode(content);
    const uint8Array = toUint8Array(json);
    const encrypted = message_encode(this.version, uint8Array, this.aes.key)
    const hex = toHex(encrypted);

    const decrypted = message_decode(encrypted, this.aes.key);
    if (uint8Array.length !== decrypted.length) {
      throw new Error(`Aes(${this.aes.key}) encrypt&decrypt error`)
    }

    for (let i = 0; i < uint8Array.length; i++) {
      if (uint8Array[i] !== decrypted[i]) {
        throw new Error(`Aes(${this.aes.key}) encrypt&decrypt error`)
      }
    }

    const hex2 = toHex(fromHex(hex));
    if (hex2 !== hex) {
      throw new Error(`Aes(${this.aes.key}) toHex(fromHex(${hex})) error`)
    }

    return hex;
  }

  decode<T extends object | string>(
    content: string,
    prototype: { new(...args: any[]): T } | T
  ): [T, Error | null] {
    const hex = fromHex(content);
    const decrypted = message_decode(hex, this.aes.key);
    const data = fromUint8Array(decrypted);
    try {
      return super.decode(data, prototype)
    } catch(e) {
      return ['' as T, new Error('')]
    }

  }
}

export function EncryptorBuilderCreator(): EncryptorBuilder {
  return new (class implements EncryptorBuilder {
    build(encrypted: Encrypted): Encryptor {
      const map: Map<Encrypted, () => Encryptor> = new Map<Encrypted, () => Encryptor>([
        [Encrypted.Json, () => new JsonEncryptor()],
        [Encrypted.AES, () => new MessageCoder()],
      ]);

      if (!map.has(encrypted)) {
        throw new Error(`not support encrypted ${encrypted}`);
      }

      return map.get(encrypted)!();
    }
  })();
}


