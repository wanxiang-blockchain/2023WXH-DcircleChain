
interface Sym {
  sym:symbol
}

export interface EventClass<T, E extends NcEvent<T>> extends Sym{
  new(ids: T[]): E
}

export class NcEvent<T> {
  // static sym = Symbol()

  constructor (ids: T[]) {
    this.ids = ids
  }

  public readonly ids: T[]
}

// not export in index.tsx
export function EventSym<T, E extends NcEvent<T>>(e: E): symbol|never {
  let ret = (e.constructor as any as Sym).sym
  if (ret === undefined) {
    throw new Error("please add 'static sym = Symbol()' in every event class")
  }

  return ret
}

let logEvent = 0
export function NewEventClass<T>() {
  return class extends NcEvent<T>{
    static readonly sym = Symbol(++logEvent)
  }
}


// the DemoEvent1 and the DemoEvent2 are different !
export const DemoEvent1 = NewEventClass<string>()

export class DemoEvent2 extends NewEventClass<string>(){}

export class DemoEvent3 extends NcEvent<string>{
  static readonly sym = Symbol()
}

