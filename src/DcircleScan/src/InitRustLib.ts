import init_crypto from "rustlib_crypto";
const promise = Promise.all([
  init_crypto()
])


export async function InitRustLib() {
  try {
    await promise
  } catch (err) {
    console.warn("InitRustLib", `err=${err}`)
    return Promise.resolve()
  }
}
