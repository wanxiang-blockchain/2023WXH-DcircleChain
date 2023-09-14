
const allResolves:Map<string, ((value:any)=>void)[]> = new Map<string, ((value: unknown) => void)[]>([]);
const allRejects:Map<string, ((value:any)=>void)[]> = new Map<string, ((value: unknown) => void)[]>([]);

export async function ExePipeline<T>(api:Function, name:string, ...args:unknown[]) {
  if (!allResolves.has(name)) {
    allResolves.set(name, []);
    allRejects.set(name, [])
  }

  const allResolve = allResolves.get(name)!;
  const allReject = allRejects.get(name)!;
  const promise = new Promise<T>((resolve, reject) => {
    allResolve.push(resolve)
    allReject.push(reject)
  })

  if (allResolve.length>1) {
    return promise;
  }

  try {
    const ret = await api(...args);
    for (let i=0; i<allResolve.length; i++) {
      allResolve[i](ret);
    }
  } catch (e) {
    for (let i=0; i<allResolve.length; i++) {
      allReject[i](e);
    }
  }

  allResolves.set(name, []);
  allRejects.set(name, [])

  return promise;
}
