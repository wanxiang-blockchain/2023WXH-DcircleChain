import MsgTextCell from "./MsgTextCell";
import MsgUnkownCell from "./MsgUnkownCell";
import MsgImageCell from "./MsgImageCell";
import MsgVideoCell from "./MsgVideoCell";
import MsgVoiceCell from "./MsgVoiceCell";
import MsgFileCell from "./MsgFileCell";
import MsgDidLinkCell from "./MsgDidLinkCell";
import MsgDidContentCell from './MsgDidContentCell';
import MsgImageTextCell from './MsgImageTextCell';
import {MsgContent, Type} from "../../helper/Message";
interface MsgCellBuilder {
  get(content:MsgContent, address:string):JSX.Element;
}
export function MsgCellBuilderCreator():MsgCellBuilder {
  return new class implements MsgCellBuilder {
    get(content: MsgContent, address:string): JSX.Element {
      const map:Map<Type, () => JSX.Element> = new Map([
        [Type.Text, () => <MsgTextCell {...content} />],
        [Type.Image, () => <MsgImageCell content={content} />],
        [Type.Video, () => <MsgVideoCell content={content} />],
        [Type.Voice, () => <MsgVoiceCell content={content} address={address} />],
        [Type.File, () => <MsgFileCell content={content} address={address} />],
        [Type.DidLink, () => <MsgDidLinkCell content={content} />],
        [Type.DidContent, () => <MsgDidContentCell content={content} address={address} />],
        [Type.ImageText, () => <MsgImageTextCell content={content} />]
      ])
      if (!map.has(content.type)) {
        return <MsgUnkownCell {...content} />;
      }
      return map.get(content.type)!();
    }
  }
}
