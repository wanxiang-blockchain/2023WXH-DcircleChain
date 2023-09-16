import {fromHex, fromUint8Array, toHex, toUint8Array} from "./Hex";
import {sha1} from "./sha1";
import {Aes} from "./Aes";
import {message_decode} from "rustlib_crypto";
import {ClassArray, Json} from "../3rdparty/ts-json";
import {DBMessage} from "../db/DBMessage";

export interface MsgContent {
  createTime:number;
  seq:number;
  msgId: string;
  senderUid:string;
  type: Type;
}
export enum Type {
  Text = 0,
  Image = 1,
  Voice = 2,
  Video = 3,
  File = 4,
  UserCard = 5,
  GroupCard = 6,
  DidLink = 7,
  DidContent = 8,
  TypeInviteToGroup = 9,
  ImageText = 10
}

export class Cover {
  bucketId:string = ''
  key:string = ''
  objectId:string = ''
}


export class Content {
  type!: Type;
}

export class Text extends Content {
  type = Type.Text;
  text: string = "";
  atUids: string[] = [];
}
export class Attachment {
  bucketId: string = ""
  objectId: string = ""
  type: string = ""
  key: string = ""
}
export class ImageAttachment extends Attachment {
  size: number = 0;
  width: number = 0;
  height: number = 0;
}

export class VideoAttachment extends ImageAttachment {
  duration: number = 0;
  type: string = '';
}

export class ImgVideoInfo {
  type: Type = Type.Image
  cover:Cover = new Cover();
  large: ImageAttachment = new ImageAttachment()
  thumb: ImageAttachment = new ImageAttachment()
  original: ImageAttachment = new ImageAttachment()
  videoOriginal: VideoAttachment = new VideoAttachment()
}

export class DidContent extends Content {
  type: Type = Type.DidContent;
  icons: ClassArray<ImageAttachment> = new ClassArray<ImageAttachment>(ImageAttachment)
  didAddress:string = '';
  title: string = '';
  abstract:string = '';
}
export class DidLink extends Content {
  type: Type = Type.DidLink;
  icon: ImageAttachment = new ImageAttachment();
  didAddress:string = '';
  title: string = '';
  url: string = '';
  desc:string = '';
}

export class File extends Content {
  type = Type.File;
  duration?: number = 0;
  bucketId: string = "";
  objectId: string = "";
  suffix: string = "";
  key: string = "";
  size: number = 0;
  name: string = '';
  atMe: boolean = false;
}

export class Image extends Content {
  type = Type.Image;
  thumb: ImageAttachment = new ImageAttachment();
  large: ImageAttachment = new ImageAttachment();
  original: ImageAttachment = new ImageAttachment();
}
export class ImageText extends Content {
  type: Type = Type.ImageText;
  text: string = '';
  attachments: string[] = [];
  imgVideoInfo:ImgVideoInfo[] = [];
}
export class Video extends Content {
  type = Type.Video;
  cover: ImageAttachment = new ImageAttachment();
  original: VideoAttachment = new VideoAttachment();
}

export class Voice extends Content {
  type: Type = Type.Voice;
  cover: ImageAttachment = new ImageAttachment();
  original: VideoAttachment = new VideoAttachment();
}

export class MsgTextContent extends Text implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgDidArticleContent extends DidContent implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgDidLinkContent extends DidLink implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgFileContent extends File implements MsgContent{
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}

export class MsgImageContent extends Image implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgImageTextContent extends ImageText implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgVideoContent extends Video implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
}
export class MsgVoiceContent extends Voice implements MsgContent {
  msgId: string = "";
  seq:number = 0;
  createTime: number = 0;
  senderUid: string = "";
  duration: number = 0;
  bucketId: string = "";
  objectId: string = "";
  size: number = 0;
  suffix: string = "";
  key: string = "";
}


export async function getDisplayMessage(list: DBMessage.Document[], essenceKey:string) {
  let messages = [];
  for(let i = 0; i < list.length; i++) {
    const item = list[i];
    const key = toHex(toUint8Array(sha1(item.chatId).substring(2, 2+16)));

    const data = new Aes(key).decrypt(fromHex(essenceKey));
    const result = fromUint8Array(data);
    const aes = new Aes(result);
    const hex = fromHex(item.content);
    const decrypted = message_decode(hex, aes.key);
    let [originData, err] = new Json().fromJson(fromUint8Array(decrypted), Content);
    if (originData.type === Type.Text) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgTextContent);
      if (err) {
        messages.push([new MsgTextContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.Image) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgImageContent);
      if (err) {
        messages.push([new MsgImageContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.Video) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgVideoContent);
      if (err) {
        messages.push([new MsgVideoContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.Voice) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgVoiceContent);
      if (err) {
        messages.push([new MsgVoiceContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.File) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgFileContent);
      if (err) {
        messages.push([new MsgFileContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.DidLink) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgDidLinkContent);
      if (err) {
        messages.push([new MsgDidLinkContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.DidContent) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgDidArticleContent);
      if (err) {
        messages.push([new MsgDidArticleContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
      continue
    }
    if (originData.type === Type.ImageText) {
      let [data1, err] = new Json().fromJson(fromUint8Array(decrypted), MsgImageTextContent);
      if (err) {
        messages.push([new MsgImageTextContent(), err])
        continue;
      }
      data1.createTime = item.createTime;
      data1.seq = item.seq;
      data1.msgId = item.msgId;
      data1.type = originData.type;
      data1.senderUid = item.creatorUid;
      messages.push([data1, null])
    }
  }

  return messages;
}
