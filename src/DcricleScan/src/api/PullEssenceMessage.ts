import {Send} from "./api";
import {ClassArray} from "../3rdparty/ts-json";
import {Encrypted} from "../helper/EncryptorBuilderFactory";
import {Type} from "../helper/Message";
import {DBMessage} from "../db/DBMessage";
import {GetDScan} from "../db/db";
import {getUs} from "../DIDBrowser";

export class PullEssenceMessageRequest {
  public chatId:string = "";
  public end:number = -1;
  public limit:number = 10;
}

export class PullEssenceMessageItem implements DBMessage.Document{
  public msgId:string = '';
  public chatId:string = '';
  public realChatId:string = "";
  public seq:number = -1;
  public content:string = '';
  public encrypted:Encrypted = Encrypted.AES;
  public creatorUid:string = '';
  public createTime:number = 0;
  public msgType:Type = Type.Text;
}

class PullEssenceMessageResponse {
  public items: ClassArray<PullEssenceMessageItem> = new ClassArray<PullEssenceMessageItem>(PullEssenceMessageItem)
}
export async function PullEssenceMessage(request: PullEssenceMessageRequest):Promise<[PullEssenceMessageResponse, Error|null]> {
  const [ret, err] = await Send("/browser/PullEssenceMessage", request, PullEssenceMessageResponse);
  if (err) {
    return [new PullEssenceMessageResponse(), err]
  }
  if (ret.items.length <= 0) {
    return [new PullEssenceMessageResponse(), null]
  }
  for(let i = 0; i < ret.items.length; i++) {
    const item = ret.items[i];
    item.realChatId = request.chatId
  }
  await DBMessage.Insert(GetDScan(), ...ret.items);
  await getUs().nc.post(new DBMessage.ChangedEvent([request.chatId]))
  return [ret, err]
}
