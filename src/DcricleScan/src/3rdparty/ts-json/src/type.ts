import {JsonDecoder, RawJson} from "./coder"

export type JsonPrimitive = number|string|boolean

export type JsonObject = {[key:string]:JsonType}

export type JsonArray = JsonType[]

export type JsonType = JsonPrimitive|JsonObject|JsonArray|null

export function isJsonArray(arg: JsonType): arg is JsonArray {
  return arg !== null && typeof arg === "object" && arg instanceof Array
}

export function isJsonObject(arg: JsonType) : arg is JsonObject {
  return arg !== null && typeof arg === "object" && !(arg instanceof Array)
}

export function isJsonObjectArray(arg: JsonType): arg is JsonObject[] {
  return  isJsonArray(arg) && arg.length === 1 && isJsonObject(arg[0])
}

export function isJsonPrimitive(arg: JsonType): arg is JsonPrimitive {
  return typeof arg === "number" || typeof arg === "string" || typeof arg === "boolean"
}

export function isJsonEmptyArray(arg: JsonType): arg is [] {
  return isJsonArray(arg) && arg.length == 0
}

export function isJsonPrimitiveArray(arg: JsonType): arg is JsonPrimitive[] {
  return isJsonArray(arg) && arg.length !== 0 && isJsonPrimitive(arg[0])
}

export type Item<Type> = Type extends Array<infer Item> ? Item : never;

type Primitive = number|null|string|symbol|boolean

type Flatten<Type> = Type extends Array<infer Item> ? Item : Type;

type RecursionCheck<T, Exclude> = ExtractClass<T> extends PropertyMustNullable<ExtractClass<T>, Exclude> ? T : never

type ExtractClass<T> = Exclude<Flatten<T>, Primitive>

type IsFunction<T> = T extends (...args: any)=>any? true : false

type CheckProperty<T, Exclude> = null extends T? (Flatten<T> extends Primitive|JsonType|JsonDecoder? T
    : RecursionCheck<T, Exclude>) : (T extends Exclude ? T : never)

export type PropertyMustNullable<T, Exclude = never> = {
  [P in keyof T]: IsFunction<T[P]> extends true? T[P] : CheckProperty<T[P], Exclude>
}

export type ProNullable<T> = { [P in keyof T]: T[P] extends JsonType | RawJson ? T[P]|null : ProNullable<T[P]>|null }

export function asNonNull<T>(arg: T): NonNullable<T> {
  return arg as NonNullable<T>
}
