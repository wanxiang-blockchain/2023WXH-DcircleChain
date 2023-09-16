import React, { Fragment, useState, memo } from 'react';
import styles from "./index.module.scss";
import { Image } from 'antd';
import { useMount } from 'ahooks';
import classNames from 'classnames';
import classnames from "classnames";
import picPlaceholder from "./image/pic_placeholder.png";
import {OssImage} from "../../oss/OssImage";
import {MsgContent, MsgDidLinkContent, Type} from "../../helper/Message";
interface Props {
  content: MsgContent
}

export class VoiceInfo {
  duration:number = 0;
  objectId:string = '';
  key:string = '';
  suffix: string = '';
}
const MsgDidLinkCell = function (props: Props):JSX.Element {
  const [data, setData] = useState<MsgDidLinkContent>(new MsgDidLinkContent());

  useMount(() => {
    if (props.content.type !== Type.DidLink) {
      throw new Error(`MsgDidLinkCell's type(${props.content.type}) invalid`)
    }
    let cont = props.content as MsgDidLinkContent;
    setData(cont);
  })


  return (
    <Fragment>
      <div className={classNames([styles.didWrap, styles.didWrapLink])}>
        <div className={styles.didLinkWrap}>
          <OssImage
            objectId={data.icon.objectId}
            objectKey={data.icon.key}
            style={{borderRadius: '7px',width: '48px', height: '48px'}} placeholder={<Image className={styles.image} src={picPlaceholder} />}/>
          <div className={styles.rightContent}>
            <div className={styles.didText}>{data.title}</div>
            <div className={styles.detailWrap}>
              <div className={styles.detailText}>查看详情</div>
              <i className={classnames([styles.icon, "iconfont icon-right"])}></i>
            </div>
          </div>
        </div>
        <div className={styles.messageWrap}>
          <div className={styles.messageLinkContent}>{data.desc}</div>
        </div>
      </div>
    </Fragment>
  )
}

export default memo(MsgDidLinkCell)
