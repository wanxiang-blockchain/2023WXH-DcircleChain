import {useMount, useUnmount} from "ahooks";
import React, { CSSProperties, useEffect, useRef, useState } from 'react';
import {Image, Modal} from "antd";
import styles from './index.module.scss'
import {LRUCache} from "../helper/LRUCache";
import {getUs} from "../DIDBrowser";
import {DBBucket} from "../db/DBBucket";
import {GetDScan} from "../db/db";
import {Downloader} from "./FileNet";
import {getLocalFile} from "./LocalFile";
import {getBase64Image} from "../helper/getBase64Image";
import {GetImageType} from "./GetImageType";
import {Aes} from "../helper/Aes";
import {ExePipeline} from "../ExePipeline";
import Progress from "../pages/component/Progress";
import {InitRustLib} from "../InitRustLib";

interface Props {
  objectId:string;
  objectKey:string;
  progress?: number;
  isScale?: boolean;
  style?:CSSProperties | undefined
  placeholder?:JSX.Element | undefined
  onClick?: (e: React.MouseEvent<HTMLDivElement>) => void;
  onCancel?: () => void;
}

const caches:LRUCache<string, string> = new LRUCache(100);
export function OssImage(props:Props) : JSX.Element {
  const [base64URL, setBase64URL] = useState("");
  const [visible, setVisible] = useState(!!props.isScale);
  const symbol = useRef(Symbol(""))
  useMount(async () => {
    await InitRustLib();
    getUs().nc.addObserver(symbol.current, DBBucket.DownloadProgressEvent, async (event) => {
      if (event.ids.indexOf(props.objectId)<0) {
        return;
      }
      const progress = await DBBucket.GetProgress(GetDScan(), [props.objectId])
      if (progress<100) {
        return ;
      }
      await loadFromDB();
    })

    await load();
  })

  const load = async () => {
    await loadFromDB();
    await loadFromServer();
  }

  useEffect( () => {
    // 为了防止当objectId更新后，无法监听到最新objectId的下载进度
    getUs().nc.removeAll(symbol.current)
    getUs().nc.addObserver(symbol.current, DBBucket.DownloadProgressEvent, async (event) => {
      if (event.ids.indexOf(props.objectId)<0) {
        return;
      }
      const progress = await DBBucket.GetProgress(GetDScan(), [props.objectId])
      if (progress<100) {
        return ;
      }
      await loadFromDB();
    })
    load().then()
  }, [props.objectId, props.objectKey])

  useUnmount(async () => {
    getUs().nc.removeAll(symbol.current)
  })

  const loadFromServer = async () => {
    // 备注：由于上层cloneElement，此时的objectId和objectKey有可能是空的
    if (props.objectId.length<=0) {
      console.warn(`loadFromServer(${props.objectId}) is empty`)
      return ;
    }

    Downloader.getInstance().addRequest(props.objectId)
  }

  async function decryptBase64Image()  {
    const [content, err] = await getLocalFile().read(props.objectId);
    if (err) {
      return ;
    }
    const aes = new Aes(props.objectKey);
    let decrypted = aes.decrypt(content);
    return getBase64Image(GetImageType(decrypted)??"jpeg", decrypted);
  }

  const loadFromDB = async () => {
    // 备注：由于上层cloneElement，此时的objectId和objectKey有可能是空的
    if (props.objectId.length<=0) {
      console.warn(`loadFromDB(${props.objectId}) is empty`)
      return ;
    }

    const value = caches.get(props.objectId)
    if (value) {
      setBase64URL(value)
      return ;
    }

    if (!await getLocalFile().has(props.objectId)) {
      console.warn(`loadFromDB(${props.objectId}) not found in local file`)
      return ;
    }

    // 同一个图片只需要解密一次
    try {
      const base64Image = await ExePipeline<string>(decryptBase64Image, props.objectId)
      setBase64URL(base64Image)
      caches.set(props.objectId, base64Image)
    } catch (e) {
      console.warn(`loadFromDB(${props.objectId}) decode error`, e)
    }
  }

  const {onClick = () => {}} = props;

  if (!props.objectId) {
    return <div>Error: objectId is empty</div>;
  }

  if (!props.objectKey) {
    return <div>Error: objectKey is empty</div>;
  }
  return <>
    {
      props.isScale ?  <div className={styles.previewImgWrap} id="previewImg">
                          <div className={styles.progress}><Progress percent={props.progress || 0} size={40} /></div>
                          {
                             base64URL.length > 0 ?
                                <Image
                                  style={props.style}
                                  src={''}
                                  onClick={onClick}
                                  preview={{
                                    visible,
                                    src: base64URL,
                                    getContainer: document.getElementById('previewImg') || document.body,
                                    onVisibleChange: (value, prevValue) => {
                                      if (!value) document.body.style.overflow = 'auto'
                                      props.onCancel && props.onCancel();
                                      setVisible(value);
                                    },
                                  }}
                                /> :
                                <Modal
                                  centered
                                  getContainer={false}
                                  closable={false}
                                  mask={false}
                                  width={'auto'}
                                  open={true}
                                  footer={null}
                                >
                                  {props.placeholder}
                                </Modal>
                          }
                        </div> :
        base64URL.length > 0 ? <Image style={props.style} src={base64URL} preview={false} onClick={onClick} /> : props.placeholder
    }
  </>
}
