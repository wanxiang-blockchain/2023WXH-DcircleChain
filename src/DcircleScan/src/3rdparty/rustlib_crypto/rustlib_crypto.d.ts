/* tslint:disable */
/* eslint-disable */
/**
* @param {Uint8Array} raw_data
* @param {string} hex_key
* @param {string} hex_iv
* @returns {Uint8Array}
*/
export function aes_encrypt(raw_data: Uint8Array, hex_key: string, hex_iv: string): Uint8Array;
/**
* @param {Uint8Array} encrypt_data
* @param {string} hex_key
* @param {string} hex_iv
* @returns {Uint8Array}
*/
export function aes_decrypt(encrypt_data: Uint8Array, hex_key: string, hex_iv: string): Uint8Array;
/**
* @param {Uint8Array} data
* @returns {number}
*/
export function parity_genera(data: Uint8Array): number;
/**
* @param {Uint8Array} data
* @param {number} parity
* @returns {boolean}
*/
export function parity_che(data: Uint8Array, parity: number): boolean;
/**
* @param {Uint8Array} address
* @param {Uint8Array} salt
* @param {Uint8Array} init_code
* @returns {Uint8Array}
*/
export function get_eip_1040_address(address: Uint8Array, salt: Uint8Array, init_code: Uint8Array): Uint8Array;
/**
* @param {Uint8Array} data
* @returns {Uint8Array}
*/
export function new_keccak256_cid(data: Uint8Array): Uint8Array;
/**
* @param {Uint8Array} data
* @returns {Uint8Array}
*/
export function new_keccak(data: Uint8Array): Uint8Array;
/**
* @param {number} version
* @param {Uint8Array} message
* @param {string} hexKey
* @returns {Uint8Array}
*/
export function message_encode(version: number, message: Uint8Array, hexKey: string): Uint8Array;
/**
* @param {Uint8Array} message
* @param {string} hexKey
* @returns {Uint8Array}
*/
export function message_decode(message: Uint8Array, hexKey: string): Uint8Array;
/**
* @param {string} hex_private_key
* @param {Uint8Array} message
* @returns {string}
*/
export function sign_for_web(hex_private_key: string, message: Uint8Array): string;
/**
* @param {string} hex_message
* @param {string} hex_signature
* @returns {string}
*/
export function recoverPublicKey(hex_message: string, hex_signature: string): string;
/**
* @param {string} hex_public_key
* @returns {string}
*/
export function recover_address_from_public_key(hex_public_key: string): string;
/**
* @param {Uint8Array} y_1
* @param {Uint8Array} y1
* @returns {Uint8Array}
*/
export function sss_y0(y_1: Uint8Array, y1: Uint8Array): Uint8Array;
/**
* @param {Uint8Array} y0
* @param {Uint8Array} y_1
* @returns {Uint8Array}
*/
export function sss_y1(y0: Uint8Array, y_1: Uint8Array): Uint8Array;
/**
* @returns {string}
*/
export function get_aes_key(): string;
/**
* @param {string} name
*/
export function greet(name: string): void;
/**
* @returns {string}
*/
export function return_string(): string;
/**
* @param {string} data
* @returns {string}
*/
export function test_type_string(data: string): string;
/**
* @param {Uint8Array} data
* @returns {Uint8Array}
*/
export function test_type_binary(data: Uint8Array): Uint8Array;
/**
* @param {bigint} data
* @returns {bigint}
*/
export function test_type_integer(data: bigint): bigint;
/**
* @param {string} hex
* @returns {string}
*/
export function base64_decode(hex: string): string;
/**
* @param {string} hex
* @returns {string}
*/
export function hex_decode(hex: string): string;
/**
* @param {Uint8Array} raw
* @returns {string}
*/
export function hex_encode(raw: Uint8Array): string;
/**
* @param {string} hex
* @returns {string}
*/
export function base64_encode(hex: string): string;
/**
* @param {Uint8Array} data
* @returns {string}
*/
export function base64_safe_encode(data: Uint8Array): string;
/**
* @param {string} data
* @returns {Uint8Array}
*/
export function base64_safe_decode(data: string): Uint8Array;
/**
* @param {number} nonce
* @param {string} from
* @param {string} to
* @param {number} opCode
* @param {bigint} signTime
* @param {Uint8Array} payload
* @param {number} chainId
* @returns {Uint8Array}
*/
export function encodeRlp(nonce: number, from: string, to: string, opCode: number, signTime: bigint, payload: Uint8Array, chainId: number): Uint8Array;
/**
* @param {number} nonce
* @param {string} from
* @param {string} to
* @param {number} opCode
* @param {bigint} signTime
* @param {Uint8Array} payload
* @param {number} chainId
* @returns {string}
*/
export function encodeRlpToHex(nonce: number, from: string, to: string, opCode: number, signTime: bigint, payload: Uint8Array, chainId: number): string;
/**
* @param {Uint8Array} encodeData
* @returns {string}
*/
export function decodeRlp(encodeData: Uint8Array): string;
/**
* @param {string} hex
* @returns {string}
*/
export function decodeRlpWithHex(hex: string): string;
/**
* @param {string} mnemonic
* @returns {boolean}
*/
export function validateMnemonic(mnemonic: string): boolean;
/**
* @param {string} word
* @returns {boolean}
*/
export function wordInWordList(word: string): boolean;
/**
* @returns {string}
*/
export function test1_include(): string;

export type InitInput = RequestInfo | URL | Response | BufferSource | WebAssembly.Module;

export interface InitOutput {
  readonly memory: WebAssembly.Memory;
  readonly aes_encrypt: (a: number, b: number, c: number, d: number, e: number, f: number, g: number) => void;
  readonly aes_decrypt: (a: number, b: number, c: number, d: number, e: number, f: number, g: number) => void;
  readonly parity_genera: (a: number, b: number) => number;
  readonly parity_che: (a: number, b: number, c: number) => number;
  readonly get_eip_1040_address: (a: number, b: number, c: number, d: number, e: number, f: number, g: number) => void;
  readonly new_keccak256_cid: (a: number, b: number, c: number) => void;
  readonly new_keccak: (a: number, b: number, c: number) => void;
  readonly message_encode: (a: number, b: number, c: number, d: number, e: number, f: number) => void;
  readonly message_decode: (a: number, b: number, c: number, d: number, e: number) => void;
  readonly sign_for_web: (a: number, b: number, c: number, d: number, e: number) => void;
  readonly recoverPublicKey: (a: number, b: number, c: number, d: number, e: number) => void;
  readonly recover_address_from_public_key: (a: number, b: number, c: number) => void;
  readonly sss_y0: (a: number, b: number, c: number, d: number, e: number) => void;
  readonly sss_y1: (a: number, b: number, c: number, d: number, e: number) => void;
  readonly get_aes_key: (a: number) => void;
  readonly greet: (a: number, b: number) => void;
  readonly return_string: (a: number) => void;
  readonly test_type_string: (a: number, b: number, c: number) => void;
  readonly test_type_binary: (a: number, b: number, c: number) => void;
  readonly base64_decode: (a: number, b: number, c: number) => void;
  readonly hex_decode: (a: number, b: number, c: number) => void;
  readonly hex_encode: (a: number, b: number, c: number) => void;
  readonly base64_encode: (a: number, b: number, c: number) => void;
  readonly base64_safe_encode: (a: number, b: number, c: number) => void;
  readonly base64_safe_decode: (a: number, b: number, c: number) => void;
  readonly encodeRlp: (a: number, b: number, c: number, d: number, e: number, f: number, g: number, h: number, i: number, j: number, k: number) => void;
  readonly encodeRlpToHex: (a: number, b: number, c: number, d: number, e: number, f: number, g: number, h: number, i: number, j: number, k: number) => void;
  readonly decodeRlp: (a: number, b: number, c: number) => void;
  readonly decodeRlpWithHex: (a: number, b: number, c: number) => void;
  readonly validateMnemonic: (a: number, b: number) => number;
  readonly wordInWordList: (a: number, b: number) => number;
  readonly test1_include: (a: number) => void;
  readonly test_type_integer: (a: number) => number;
  readonly rustsecp256k1_v0_4_1_context_create: (a: number) => number;
  readonly rustsecp256k1_v0_4_1_context_destroy: (a: number) => void;
  readonly rustsecp256k1_v0_4_1_default_illegal_callback_fn: (a: number, b: number) => void;
  readonly rustsecp256k1_v0_4_1_default_error_callback_fn: (a: number, b: number) => void;
  readonly __wbindgen_add_to_stack_pointer: (a: number) => number;
  readonly __wbindgen_malloc: (a: number) => number;
  readonly __wbindgen_realloc: (a: number, b: number, c: number) => number;
  readonly __wbindgen_free: (a: number, b: number) => void;
}

export type SyncInitInput = BufferSource | WebAssembly.Module;
/**
* Instantiates the given `module`, which can either be bytes or
* a precompiled `WebAssembly.Module`.
*
* @param {SyncInitInput} module
*
* @returns {InitOutput}
*/
export function initSync(module: SyncInitInput): InitOutput;

/**
* If `module_or_path` is {RequestInfo} or {URL}, makes a request and
* for everything else, calls `WebAssembly.instantiate` directly.
*
* @param {InitInput | Promise<InitInput>} module_or_path
*
* @returns {Promise<InitOutput>}
*/
export default function __wbg_init (module_or_path?: InitInput | Promise<InitInput>): Promise<InitOutput>;
