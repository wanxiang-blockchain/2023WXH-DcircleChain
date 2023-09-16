
export class Utf8 {
  public readonly raw: Uint8Array;
  private readonly indexes: Array<number>;
  private str:string|null;
  public readonly byteLength:number;
  public readonly length:number;

  constructor(input: ArrayBuffer|string) {
    this.indexes = new Array<number>();

    if (typeof input !== "string") {
      this.raw = new Uint8Array(input);
      let utf8i = 0;
      while (utf8i < this.raw.length) {
        this.indexes.push(utf8i);
        utf8i += Utf8.getUTF8CharLength(Utf8.loadUTF8CharCode(this.raw, utf8i));
      }
      this.indexes.push(utf8i);  // end flag

      this.str = null;

    } else {
      this.str = input;

      let length = 0;
      for (let ch of input) {
        length += Utf8.getUTF8CharLength(ch.codePointAt(0)!)
      }
      this.raw = new Uint8Array(length);

      let index = 0;
      for (let ch of input) {
        this.indexes.push(index);
        index = Utf8.putUTF8CharCode(this.raw, ch.codePointAt(0)!, index)
      }
      this.indexes.push(index); // end flag
    }

    this.length = this.indexes.length - 1;
    this.byteLength = this.raw.byteLength;

  }

  private static loadUTF8CharCode(aChars: Uint8Array, nIdx: number): number {

    let nLen = aChars.length, nPart = aChars[nIdx];

    return nPart > 251 && nPart < 254 && nIdx + 5 < nLen ?
      /* (nPart - 252 << 30) may be not safe in ECMAScript! So...: */
      /* six bytes */ (nPart - 252) * 1073741824 + (aChars[nIdx + 1] - 128 << 24)
      + (aChars[nIdx + 2] - 128 << 18) + (aChars[nIdx + 3] - 128 << 12)
      + (aChars[nIdx + 4] - 128 << 6) + aChars[nIdx + 5] - 128
      : nPart > 247 && nPart < 252 && nIdx + 4 < nLen ?
        /* five bytes */ (nPart - 248 << 24) + (aChars[nIdx + 1] - 128 << 18)
        + (aChars[nIdx + 2] - 128 << 12) + (aChars[nIdx + 3] - 128 << 6)
        + aChars[nIdx + 4] - 128
        : nPart > 239 && nPart < 248 && nIdx + 3 < nLen ?
          /* four bytes */(nPart - 240 << 18) + (aChars[nIdx + 1] - 128 << 12)
          + (aChars[nIdx + 2] - 128 << 6) + aChars[nIdx + 3] - 128
          : nPart > 223 && nPart < 240 && nIdx + 2 < nLen ?
            /* three bytes */ (nPart - 224 << 12) + (aChars[nIdx + 1] - 128 << 6)
            + aChars[nIdx + 2] - 128
            : nPart > 191 && nPart < 224 && nIdx + 1 < nLen ?
              /* two bytes */ (nPart - 192 << 6) + aChars[nIdx + 1] - 128
              :
              /* one byte */ nPart;
  }

  private static putUTF8CharCode(aTarget: Uint8Array, nChar: number
                                 , nPutAt: number):number {

    let nIdx = nPutAt;

    if (nChar < 0x80 /* 128 */) {
      /* one byte */
      aTarget[nIdx++] = nChar;
    } else if (nChar < 0x800 /* 2048 */) {
      /* two bytes */
      aTarget[nIdx++] = 0xc0 /* 192 */ + (nChar >>> 6);
      aTarget[nIdx++] = 0x80 /* 128 */ + (nChar & 0x3f /* 63 */);
    } else if (nChar < 0x10000 /* 65536 */) {
      /* three bytes */
      aTarget[nIdx++] = 0xe0 /* 224 */ + (nChar >>> 12);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 6) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + (nChar & 0x3f /* 63 */);
    } else if (nChar < 0x200000 /* 2097152 */) {
      /* four bytes */
      aTarget[nIdx++] = 0xf0 /* 240 */ + (nChar >>> 18);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 12) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 6) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + (nChar & 0x3f /* 63 */);
    } else if (nChar < 0x4000000 /* 67108864 */) {
      /* five bytes */
      aTarget[nIdx++] = 0xf8 /* 248 */ + (nChar >>> 24);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 18) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 12) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 6) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + (nChar & 0x3f /* 63 */);
    } else /* if (nChar <= 0x7fffffff) */ { /* 2147483647 */
      /* six bytes */
      aTarget[nIdx++] = 0xfc /* 252 */ + /* (nChar >>> 30) may be not safe in ECMAScript! So...: */ (nChar / 1073741824);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 24) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 18) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 12) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + ((nChar >>> 6) & 0x3f /* 63 */);
      aTarget[nIdx++] = 0x80 /* 128 */ + (nChar & 0x3f /* 63 */);
    }

    return nIdx;

  };

  private static getUTF8CharLength(nChar: number): number {
    return nChar < 0x80 ? 1 : nChar < 0x800 ? 2 : nChar < 0x10000
      ? 3 : nChar < 0x200000 ? 4 : nChar < 0x4000000 ? 5 : 6;
  }


  // private static loadUTF16CharCode(aChars: Uint16Array, nIdx: number): number {
  //
  //   /* UTF-16 to DOMString decoding algorithm */
  //   let nFrstChr = aChars[nIdx];
  //
  //   return nFrstChr > 0xD7BF /* 55231 */ && nIdx + 1 < aChars.length ?
  //     (nFrstChr - 0xD800 /* 55296 */ << 10) + aChars[nIdx + 1] + 0x2400 /* 9216 */
  //     : nFrstChr;
  // }
  //
  // private static putUTF16CharCode(aTarget: Uint16Array, nChar: number, nPutAt: number):number {
  //
  //   let nIdx = nPutAt;
  //
  //   if (nChar < 0x10000 /* 65536 */) {
  //     /* one element */
  //     aTarget[nIdx++] = nChar;
  //   } else {
  //     /* two elements */
  //     aTarget[nIdx++] = 0xD7C0 /* 55232 */ + (nChar >>> 10);
  //     aTarget[nIdx++] = 0xDC00 /* 56320 */ + (nChar & 0x3FF /* 1023 */);
  //   }
  //
  //   return nIdx;
  // }
  //
  // private static getUTF16CharLength(nChar: number): number {
  //   return nChar < 0x10000 ? 1 : 2;
  // }

  public toString():string {
    if (this.str != null) {
      return this.str
    }

    let codes = new Array<number>();
    for (let utf8i = 0; utf8i < this.raw.length;) {
      let code = Utf8.loadUTF8CharCode(this.raw, utf8i);
      codes.push(code);
      utf8i += Utf8.getUTF8CharLength(code);
    }

    try {
      this.str = String.fromCodePoint(...codes);
    } catch (e) {
      this.str = "";
      const chunk = 8 * 1024;
      let i;
      for (i = 0; i < codes.length / chunk; i++) {
        this.str += String.fromCharCode.apply(null, codes.slice(i * chunk, (i + 1) * chunk));
      }
      this.str += String.fromCharCode.apply(null, codes.slice(i * chunk));
      this.str = decodeURIComponent(this.str);
    }

    return this.str;
  }

  public codePointAt(index: number):ArrayBuffer {
    return this.raw.slice(this.indexes[index], this.indexes[index+1]);
  }

}


