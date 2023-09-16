export function GetImageType(buffer: ArrayBuffer): string|null {
  const uint8Array = new Uint8Array(buffer);
  const byte1 = uint8Array[0];
  const byte2 = uint8Array[1];
  const byte3 = uint8Array[2];
  const byte4 = uint8Array[3];

  if (byte1 === 0xFF && byte2 === 0xD8 && byte3 === 0xFF) {
    return 'jpeg';
  }
  if (byte1 === 0x89 && byte2 === 0x50 && byte3 === 0x4E && byte4 === 0x47) {
    return 'png';
  }
  if (byte1 === 0x47 && byte2 === 0x49 && byte3 === 0x46) {
    return 'gif';
  }
  if (byte1 === 0x42 && byte2 === 0x4D) {
    return 'bmp';
  }
  if (byte1 === 0x52 && byte2 === 0x49 && byte3 === 0x46 && byte4 === 0x46) {
    return 'webp';
  }

  return null;
}
