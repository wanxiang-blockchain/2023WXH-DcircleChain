// 接收16进制的字符串
export const fromHex = (hex:string) => new Uint8Array(hex.match(/.{1,2}/g)!.map(byte => parseInt(byte, 16)));

export const toHex = (array:Uint8Array) => Array.from(array, i => i.toString(16).padStart(2, "0")).join("");

export const toUint8Array = (data:string):Uint8Array => {
  const encoder = new TextEncoder();
  return encoder.encode(data);
}

export const fromUint8Array = (data:Uint8Array):string => {
  const decoder = new TextDecoder();
  return decoder.decode(data);
}
