export default async function runAfter(duration:number) {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(null);
    }, duration)
  })
}
