import {JsonType} from "./type"

export interface ConstructorJsonDecoder {
  decodeJson(json: JsonType): [any, Error|null]
}

export interface ConstructorJsonEncoder {
  encodeJson<T>(instance: T): JsonType
}

export interface JsonDecoder {
  decodeJson(json: JsonType): Error|null
}

export interface JsonEncode {
  encodeJson(): JsonType
}

export class RawJson implements JsonDecoder, JsonEncode{
  public raw:JsonType = null

  decodeJson(json: JsonType): Error | null {
    this.raw = json
    return null;
  }

  encodeJson(): JsonType | null {
    return this.raw;
  }
}

export function hasConstructorDecoder(constructor: object): constructor is ConstructorJsonDecoder {
  let con = constructor as any as ConstructorJsonDecoder
  return con.decodeJson !== undefined && typeof con.decodeJson === "function"
    && con.decodeJson.length === 1
}

export function hasConstructorEncoder(constructor: object): constructor is ConstructorJsonEncoder {
  let con = constructor as any as ConstructorJsonEncoder
  return con.encodeJson !== undefined && typeof con.encodeJson === "function"
    && con.encodeJson.length === 1
}

export function hasDecoder(self: object): self is JsonDecoder {
  let sf = self as any as JsonDecoder
  return sf.decodeJson !== undefined && typeof sf.decodeJson === "function"
    && sf.decodeJson.length === 1
}

export function hasEncoder(self: object): self is JsonEncode {
  let sf = self as any as JsonEncode
  return sf.encodeJson !== undefined && typeof sf.encodeJson === "function"
    && sf.encodeJson.length === 0
}
