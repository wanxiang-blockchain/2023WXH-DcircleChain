
export function getBase64Image(type:string, uint8Array: Uint8Array): string {
  let binary = '';
  const len = uint8Array.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(uint8Array[i]);
  }

  return `data:image/${type};base64,${btoa(binary)}`

}
