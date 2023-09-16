import React, { Fragment, memo, useState } from 'react';
import styles from './index.module.scss';
import { useMount } from 'ahooks';
import classnames from 'classnames';
import ImageShow from '../component/ImageShow';
import {Cover, ImageAttachment, MsgContent, MsgImageTextContent, Type, VideoAttachment} from "../../helper/Message";

interface Props {
  content: MsgContent
  stopClick?:boolean
  width?: string
}



const MsgImageTextCell = function (props: Props):JSX.Element {
  const [data, setData] = useState<MsgImageTextContent>(new MsgImageTextContent());

  useMount(() => {
    if (props.content.type !== Type.ImageText) {
      throw new Error(`MsgDidContentCell's type(${props.content.type}) invalid`)
    }
    let cont = props.content as MsgImageTextContent;
    cont.imgVideoInfo = cont.attachments.map(item => {
      const it = JSON.parse(item);
      return {
        type: it.type,
        cover: it.cover ? it.cover : new Cover(),
        thumb: it.thumb ? it.thumb : new ImageAttachment(),
        large: it.large ? it.large : new ImageAttachment(),
        original: it.original ? it.original : new ImageAttachment(),
        videoOriginal: it.original ? it.original : new VideoAttachment()
      }
    })
    setData(cont);
  })

  return (
    <Fragment>
      <div style={{width: props.width}} className={classnames([styles.imageTextWrap, styles.imgWrap])}>
        <div className={styles.title}>{data.text}</div>
        <div className={styles.imagesWrap}>
          <ImageShow stopClick={props.stopClick} attachments={data.imgVideoInfo}></ImageShow>
        </div>
      </div>
    </Fragment>
  )
}

export default memo(MsgImageTextCell)
