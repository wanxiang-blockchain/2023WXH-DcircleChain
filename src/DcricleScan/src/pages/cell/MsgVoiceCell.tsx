import React, {Fragment, useState, memo} from 'react';
import styles from "./index.module.scss";
import { AudioPlayer } from './conponents/Voice';
import { useMount } from 'ahooks';
import {MsgContent, MsgVoiceContent, Type} from "../../helper/Message";
interface Props {
  content: MsgContent;
  address: string;
}

export class VoiceInfo {
  duration:number = 0;
  objectId:string = '';
  key:string = '';
  suffix: string = '';
}
const MsgVoiceCell = function (props: Props):JSX.Element {
  const [data, setData] = useState<MsgVoiceContent>(new MsgVoiceContent());

  useMount(() => {
    if (props.content.type !== Type.Voice) {
      throw new Error(`MsgVoiceCell's type(${props.content.type}) invalid`)
    }
    let cont = props.content as MsgVoiceContent;
    setData(cont);
  })
  return (
    <Fragment>
      <div className={styles.voiceWrap}>
        <AudioPlayer address={props.address} msgId={props.content.msgId} voiceInfo={{
          duration: (props.content as MsgVoiceContent).duration,
          objectId: (props.content as MsgVoiceContent).objectId,
          key: (props.content as MsgVoiceContent).key,
          suffix: (props.content as MsgVoiceContent).suffix
        }} />
      </div>
    </Fragment>
  )
}

export default memo(MsgVoiceCell)
