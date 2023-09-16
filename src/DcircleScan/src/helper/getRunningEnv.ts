export function getIsDcircleEnv():boolean {
  let userAgent = window.navigator.userAgent.toLowerCase();
  return userAgent.includes("dcircle")
}

export function getIsWxEnv():boolean {
  let userAgent = window.navigator.userAgent.toLowerCase();
  return userAgent.includes("micromessenger")
}
