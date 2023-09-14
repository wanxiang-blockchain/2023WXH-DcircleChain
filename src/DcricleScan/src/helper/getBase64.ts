export function decodeBase64(base64UrlString: string): Uint8Array {
  let base64String = base64UrlString;
  base64String = base64String.replace(/-/g, "+");
  base64String = base64String.replace(/_/g, "/");

  const paddingLength = 4 - (base64String.length % 4);
  if (paddingLength < 4) {
    base64String = base64String.padEnd(base64String.length + paddingLength, "=");
  }

  const decodedData = atob(base64String);
  const uint8Array = new Uint8Array(decodedData.length);

  for (let i = 0; i < decodedData.length; i++) {
    uint8Array[i] = decodedData.charCodeAt(i);
  }

  return uint8Array;
}
