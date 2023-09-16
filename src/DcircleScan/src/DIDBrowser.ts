import {AloneUserSpace, MemoryStorage, UserSpace} from "./3rdparty/ts-baselib";
import {WSSUrl} from "./config";

let userSpace: UserSpace = new AloneUserSpace(new MemoryStorage(), WSSUrl);

export function setUs(us:UserSpace) {
  userSpace = us
}

export function getUs():UserSpace {
  return userSpace;
}