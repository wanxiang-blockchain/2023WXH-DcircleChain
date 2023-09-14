

import {
  canRecEmptyArray,
  getArrayItemPrototype, isClass, isClassArray, isPrimitiveArray
} from "./class"
import {hasConstructorDecoder, hasConstructorEncoder, hasDecoder, hasEncoder} from "./coder"
import {
  isJsonEmptyArray,
  isJsonObject,
  isJsonObjectArray, isJsonPrimitiveArray,
  JsonObject,
  JsonType,
} from "./type"

const jsonToPropertySym: unique symbol = Symbol("from-json");
const propertyToJsonSym: unique symbol = Symbol("to-json");
// const jsonDecoderSym:symbol = Symbol("json-decoder");
// const jsonEncoderSym:symbol = Symbol("json-encoder");

type JsonToPropertyMap = Map<string, string|symbol>
type PropertyToJsonMap = Map<string|symbol, string>

interface ConverterMap {
  [jsonToPropertySym]?: JsonToPropertyMap
  [propertyToJsonSym]?: PropertyToJsonMap
}

// 清空原来的非对象(数组)值
// TODO: for in 目前查找的资料只是会遍历出可枚举的，同时查得对象的方法是不可枚举的，但是
// 这里出现了 for in 遍历出了对象的方法。（es5的浏览器环境出现此现象，其他编译方式与运行环境未验证）
// 所以这里加了“冗余”的条件判断
function getPropertyKeys<T extends object>(instance: T): (keyof T)[]{
  let keys:(keyof T)[] = []
  for (let p in instance) {
    if (instance.hasOwnProperty(p) && instance.propertyIsEnumerable(p)) {
      keys.push(p)
    }
  }
  return keys
}

function isPropertyKey<T extends object>(instance: T, key: string|symbol|number): key is keyof T {
  return instance.hasOwnProperty(key) && instance.propertyIsEnumerable(key)
}

const has = Symbol("has")

// todo: generic error: for example  class a<T>{d:T}    has<a> ?= {} not {d:boolean}
// export type Has<T> = {[P in keyof T as (T[P] extends Function ? never : P)]: boolean}
export type Has<T> = {[P in keyof T]: boolean}

export function JsonHas<T extends object>(arg: T): Has<T> {
  if (arg.hasOwnProperty(has)) {
    return (arg as any)[has]
  }

  // 仅仅是补偿性逻辑，fromJson 返回的对象都已经设置了has
  let ret:{[p: string]:boolean} = {}
  for (let p in arg) {
    ret[p] = true
  }

  Object.defineProperty(arg, has, {enumerable:false, value:ret, writable:false})

  return ret as Has<T>
}

export class Json {

  constructor() {
    this.disallowNull()
  }

  public ignoreNull(): this {
    this.nullToJson = (_,_2)=>{}
    this.fromNullJson = (_,_2)=>{return null}
    return this
  }

  public allowNull(): this {
    this.nullToJson = (p,key)=>{(p as any)[key] = null}
    this.fromNullJson = (p,key)=>{(p as any)[key] = null; return null}
    return this
  }

  public disallowNull(): this {
    this.nullToJson = (_,_2)=>{}
    this.fromNullJson = (_,_2)=>{return Error("can not null")}
    return this
  }

  private nullToJson: <T>(to:T, key: keyof T) =>void = (_,_2)=>{}
  private fromNullJson: <T>(to:T, key: keyof T) =>Error|null = (_,_2)=>{return null}

  public toJson<T extends object>(instance: T): string {

    let to = this.class2json(instance);

    return JSON.stringify(to);
  }

  private class2json<T extends object>(from: T): JsonType {
    if (hasEncoder(from)) {
      return from.encodeJson()
    }
    if (hasConstructorEncoder(from.constructor)) {
      return from.constructor.encodeJson(from)
    }

    let property2jsonMap: PropertyToJsonMap = (from as ConverterMap)[propertyToJsonSym] || new Map();

    let to:{[key:string]:any} = {}

    for (let key of getPropertyKeys(from)) {
      let toKey = property2jsonMap.get(key as string|symbol) || key as string;
      if (toKey === "-") {
        continue
      }

      let fromV = from[key]

      if (fromV === undefined) {
        continue
      }

      if (fromV === null) {
        this.nullToJson(to, toKey)
        continue
      }

      if (isClass(fromV)) {
        to[toKey] = this.class2json(fromV);
        continue;
      }

      if (isClassArray(fromV)) {
        let arr: JsonType[] = []
        for (let item of fromV) {
          arr.push(this.class2json(item))
        }
        to[toKey] = arr
        continue
      }

      // 基本变量赋值
      to[toKey] = fromV;
    }

    return to
  }

  public fromJson<T extends {[P in keyof T]:T[P]}>(json: JsonObject|string
    , prototype: {new(...args:any[]): T}|T):[T, null|Error] {

    if (typeof prototype === "function") {
      prototype = new prototype();
    }

    let jsonObj :JsonObject = json as JsonObject
    if (typeof json === "string") {
      let par = JSON.parse(json)
      if (par === null || typeof par !== "object" || par instanceof Array) {
        return [prototype, new Error("json string must be '{...}'")]
      }

      jsonObj = par
    }

    return this.json2class(jsonObj, prototype, prototype.constructor.name)
  }

  private json2class<T extends {[n:number]:any}>(from: JsonObject, prototype: T
    , className: string): [T, null|Error] {

    if (hasDecoder(prototype)) {
      let err = prototype.decodeJson(from)
      return [prototype, err]
    }
    if (hasConstructorDecoder(prototype.constructor)) {
      return prototype.constructor.decodeJson(from)
    }

    let json2PropertyMap: JsonToPropertyMap = (prototype as ConverterMap)[jsonToPropertySym] || new Map();
    let property2jsonMap: PropertyToJsonMap = (prototype as ConverterMap)[propertyToJsonSym] || new Map();

    let hasSetKey = new Set<keyof typeof prototype>()

    let hasValue:{[p: string|symbol|number]:boolean} = {}

    for (let key of getPropertyKeys(from)) {
      if (key === "-") {
        continue
      }

      let toKey = json2PropertyMap.get(key as string) || key;

      if (property2jsonMap.get(toKey as string|symbol) === "-") {
        continue
      }

      // class对象没有这项值，就跳过
      if (!isPropertyKey(prototype, toKey)) {
        continue
      }

      hasSetKey.add(toKey)
      hasValue[toKey] = true

      let propertyName = className + "." + toKey.toString()
      if (from[key] === null) {
        let err = this.fromNullJson(prototype, toKey)
        if (err) {
          return [prototype, Error(propertyName + "---" + err.message)]
        }
        continue
      }

      let fromV = from[key]
      let keyProto = prototype[toKey]

      let err = checkType(fromV, keyProto, propertyName)
      if (err !== null) {
        return [prototype, err]
      }

      if (isJsonObjectArray(fromV) && isClassArray<{[key:number]:any}>(keyProto)) {
        let item = getArrayItemPrototype(keyProto)
        let retArr = new Array<typeof item>()
        for (let i = 0; i < fromV.length; ++i) {
          let [ret, err] = this.json2class(fromV[i], item, propertyName + `[${i}]`)
          if (err !== null) {
            return [prototype, err]
          }
          retArr.push(ret)
        }

        prototype[toKey] = retArr
        continue
      }

      if (isJsonObject(fromV) && isClass(keyProto)) {
        [prototype[toKey], err] = this.json2class(fromV, keyProto, propertyName)
        if (err !== null) {
          return [prototype, err]
        }
        continue
      }

      prototype[toKey] = fromV
    }

    for (let key of getPropertyKeys(prototype)) {
      if (!hasSetKey.has(key)) {
        // (prototype as ProNullable<typeof prototype>)[key] = null
        hasValue[key] = false
      }
    }

    Object.defineProperty(prototype, has, {enumerable:false, value:hasValue, writable:false})

    return [prototype, null]
  }

  // <T extends Mull<T, Exclude>, Exclude = never>
  // public fromJson2<T extends {[P in keyof T]:T[P]}>(json: JsonObject|string
  //   , prototype: {new(...args:any[]): T}|T):[ProNullable<T>, null|Error] {
  //
  //   if (typeof prototype === "function") {
  //     prototype = new prototype();
  //   }
  //
  //   let jsonObj :JsonObject = json as JsonObject
  //   if (typeof json === "string") {
  //     let par = JSON.parse(json)
  //     if (par === null || typeof par !== "object" || par instanceof Array) {
  //       return [prototype, new Error("json string must be '{...}'")]
  //     }
  //
  //     jsonObj = par
  //   }
  //
  //   return this.json2class2(jsonObj, prototype, prototype.constructor.name)
  // }
  //
  // private json2class2<T extends {[n:number]:any}>(from: JsonObject, prototype: T
  //                                       , className: string): [ProNullable<T>, null|Error] {
  //
  //   if (hasDecoder(prototype)) {
  //     let err = prototype.decodeJson(from)
  //     return [prototype, err]
  //   }
  //   if (hasConstructorDecoder(prototype.constructor)) {
  //     return prototype.constructor.decodeJson(from)
  //   }
  //
  //   let json2PropertyMap: JsonToPropertyMap = (prototype as ConverterMap)[jsonToPropertySym] || new Map();
  //   let property2jsonMap: PropertyToJsonMap = (prototype as ConverterMap)[propertyToJsonSym] || new Map();
  //
  //   let hasSetKey = new Set<keyof typeof prototype>()
  //
  //   for (let key of getPropertyKeys(from)) {
  //     if (key === "-") {
  //       continue
  //     }
  //
  //     let toKey = json2PropertyMap.get(key as string) || key;
  //
  //     if (property2jsonMap.get(toKey as string|symbol) === "-") {
  //       continue
  //     }
  //
  //     // class对象没有这项值，就跳过
  //     if (!isPropertyKey(prototype, toKey)) {
  //       continue
  //     }
  //
  //     hasSetKey.add(toKey)
  //
  //     if (from[key] === null) {
  //       prototype[toKey] = null
  //       continue
  //     }
  //
  //     className = className + "." + toKey.toString()
  //
  //     let fromV = from[key]
  //     let keyProto = prototype[toKey]
  //
  //     let err = checkType(fromV, keyProto, className)
  //     if (err !== null) {
  //       return [prototype, err]
  //     }
  //
  //     if (isJsonObjectArray(fromV) && isClassArray<{[key:number]:any}>(keyProto)) {
  //       let item = getArrayItemPrototype(keyProto)
  //       let retArr = new Array<typeof item>()
  //       for (let i = 0; i < fromV.length; ++i) {
  //         let [ret, err] = this.json2class(fromV[i], item, className + `[${i}]`)
  //         if (err !== null) {
  //           return [prototype, err]
  //         }
  //         retArr.push(ret)
  //       }
  //
  //       prototype[toKey] = retArr
  //       continue
  //     }
  //
  //     if (isJsonObject(fromV) && isClass(keyProto)) {
  //       [prototype[toKey], err] = this.json2class(fromV, keyProto, className)
  //       if (err !== null) {
  //         return [prototype, err]
  //       }
  //       continue
  //     }
  //
  //     prototype[toKey] = fromV
  //   }
  //
  //   for (let key of getPropertyKeys(prototype)) {
  //     if (!hasSetKey.has(key)) {
  //       (prototype as ProNullable<typeof prototype>)[key] = null
  //     }
  //   }
  //
  //   return [prototype, null]
  // }
}

// '-' : ignore
export function JsonKey(jsonKey:string, ...jsonKeys:string[]): PropertyDecorator {
  return (target: object, propertyKey: string|symbol) => {

    let targetSym = target as ConverterMap

    if (!targetSym[jsonToPropertySym]) {
      targetSym[jsonToPropertySym] = new Map();
    }
    targetSym[jsonToPropertySym].set(jsonKey, propertyKey);
    for (let key of jsonKeys) {
      targetSym[jsonToPropertySym].set(key, propertyKey);
    }

    if (!targetSym[propertyToJsonSym]) {
      targetSym[propertyToJsonSym] = new Map();
    }
    targetSym[propertyToJsonSym].set(propertyKey, jsonKey);
  }
}

/*
* todo:
* 普通的类
* 数组中的值的类型必须一致
* 数组中的值不能有null
* 不能有高维数组
* 数组中可以有类
*
* */
function checkType<T>(fromV: JsonType
  , property: T[keyof T]|null, className: string): Error|null {

  if (fromV === null) {
    return null
  }

  if (isJsonObject(fromV) /* {} */ && !isClass(property) /* not init by new XXX(...)*/) {
    return TypeError(`the json value is '{}', but the property of ${className} is not. 
        Please init the value with "new XXX(...)"`)
  }

  if (isJsonObjectArray(fromV) /* [{}] */ && !isClassArray(property) /* not init by new ClassArray*/){
    return TypeError(`the json value is '[{}]', but the property of ${className} is not. 
        Please init the value with "new ClassArray(clazz)"`)
  }
  // todo: check array element

  if (property === null || property === undefined) {
    return null
  }

  if (isJsonPrimitiveArray(fromV) && !isPrimitiveArray(property)) {
    return TypeError(`the json value is '[number|string|boolean]', but the property of ${className} is not. 
        Please init the value with "null or [xxx]"`)
  }
  // todo: check array element

  if (isJsonEmptyArray(fromV) && !canRecEmptyArray(property)) {
    return TypeError(`the json value is '[]', but the property of ${className} is not array type.`)
  }

  if (typeof fromV !== typeof property) {
    return TypeError(`the json value is "<${typeof fromV}>${fromV}", but the property of ${className} is '<${typeof property}>${property}'.
        Please init the value with "null or <${typeof fromV}>"`)
  }

  return null
}


