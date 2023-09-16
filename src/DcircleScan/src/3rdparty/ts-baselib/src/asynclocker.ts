

export class AsyncLocker {
  private lockers = new Map<string, (()=>void)[]>()

  async lock(name: string = "__default"): Promise<void> {
    return new Promise((resolve)=>{
      let ls = this.lockers.get(name)
      if (ls !== undefined) {
        ls.push(resolve)
        this.lockers.set(name, ls)
        return
      }

      this.lockers.set(name, [])
      resolve()
    })
  }

  unlock(name: string = "__default") {
    let ls = this.lockers.get(name)
    if (ls === undefined) {
      console.warn("unlock the name which be not locked")
      return
    }

    let one = ls.shift()

    if (one === undefined) {
      this.lockers.delete(name)
      return
    }

    one()
  }
}