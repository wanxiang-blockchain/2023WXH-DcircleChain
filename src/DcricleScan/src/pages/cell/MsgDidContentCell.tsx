import React, {Fragment, memo, useRef, useState} from 'react';
import styles from './index.module.scss';
import { Image } from 'antd';
import { useMount } from 'ahooks';
import picPlaceholder from './image/pic_placeholder.png';
import didImg from './image/didImg.png';
import { OssImage } from '../../oss/OssImage';
import replaceMiddleWithDots from '../../helper/replaceMiddleWithDots';
import {MsgContent, MsgDidArticleContent, Type} from "../../helper/Message";
import {getIsDcircleEnv, getIsWxEnv} from "../../helper/getRunningEnv";
import copy from "copy-to-clipboard";
import {getHttp, getNavigate} from "../../helper/handleUrl";
import {AppScheme} from "../../config";
import {DBGroup} from "../../db/DBGroup";
import {getUs} from "../../DIDBrowser";
import {Async} from "../../Async";
import {BusHelper} from "../../helper/BusHelper";

interface Props {
  content: MsgContent
  address: string
}

const MsgDidContentCell = function (props: Props):JSX.Element {
  const [data, setData] = useState<MsgDidArticleContent>(new MsgDidArticleContent());
  const timers = useRef<NodeJS.Timer>()

  useMount(() => {
    if (props.content.type !== Type.DidContent) {
      throw new Error(`MsgDidContentCell's type(${props.content.type}) invalid`)
    }
    let cont = props.content as MsgDidArticleContent;
    setData(cont);
  })
  const toDownload = () => {
    // 微信中打开，直接弹窗引导在浏览器中打开
    if (getIsWxEnv()) {
      Async(async () => {
        await getUs().nc.post(new DBGroup.ModalChangedEvent(['true']))
      })
      return;
    }

    copy(getHttp())

    document.addEventListener("visibilitychange", function() {
      if (document.hidden) {
        clearTimeout(timers.current);
      }
    });

    const url = getHttp();
    let link = document.createElement('a');
    link.href = `${AppScheme}/app?url=${url}`;
    link.click()

    timers.current = setTimeout(() => {
      const route = getNavigate()
      route && route(`/download`, {state: {click: 'notAllow'}});
    }, 3000)
  }
  async function openPage() {
    if (getIsDcircleEnv()) {
      await BusHelper.HandleDidNcApp(props.address, data.didAddress, data.msgId);
      return
    }
    toDownload();
  }



  return (
    <Fragment>
      <div className={styles.didWrap} onClick={openPage}>
        <div className={styles.didImgWrap}>
          <Image preview={false} className={styles.didImg} src={didImg}></Image>
        </div>
        <div className={styles.messageContent} style={{color: '#fff'}}>{data.title}</div>
        <div className={styles.didContent}>{replaceMiddleWithDots(data.didAddress)}</div>
        <div className={styles.didContentItemWrap}>
          <div className={styles.didText}>{data.abstract}</div>
          <div className={styles.didImgList}>
            {
              data.icons.map(item => (<div className={styles.didImgItem} key={item.objectId}>
                <OssImage
                  objectId={item.objectId}
                  objectKey={item.key}
                  style={{borderRadius: '4px',width: '48px', height: '48px'}} placeholder={<Image className={styles.image} src={picPlaceholder} />}/>
              </div>))
            }
          </div>
        </div>
      </div>
    </Fragment>
  )
}

export default memo(MsgDidContentCell)
