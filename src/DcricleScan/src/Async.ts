// 明确指定f异步执行，如果f中有await的操作，Async()后面的代码不会等待f全部执行结束才执行，
// 类似于放到后台异步执行的意思，不阻塞后续的代码
export function Async(f: ()=>Promise<void>) {
  f().then();
}
