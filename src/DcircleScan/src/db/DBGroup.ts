import {NcEvent} from "../3rdparty/ts-nc";

export namespace DBGroup {
  export enum Fields {
    id = "id"
  }
  export interface Document {

  }
  export class ModalChangedEvent extends NcEvent<string> {
    static sym = Symbol();
  }
}
