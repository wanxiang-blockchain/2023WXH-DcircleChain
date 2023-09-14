import {v4 as uuidv4} from "uuid";
import CryptoJS from "crypto-js";

export function sha1(data = uuidv4()):string {
  return `${CryptoJS.SHA1(data).toString().toLowerCase()}`
}
