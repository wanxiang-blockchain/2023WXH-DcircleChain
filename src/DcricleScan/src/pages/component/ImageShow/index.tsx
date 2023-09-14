import { Image } from 'antd';
import styles from './index.module.scss'
import classNames from 'classnames';
import deleteBtn from '../../../images/delete.png';
import React, { useImperativeHandle, useRef, useState, useEffect } from 'react';
import {OssImage} from "../../../oss/OssImage";
import OSSVideoView from '../../../oss/OSSVideoView';
import NoImage from '../NoImage';
import {ImgVideoInfo, Type} from "../../../helper/Message";
import {getUs} from "../../../DIDBrowser";
import {DBBucket} from "../../../db/DBBucket";
import {GetDScan} from "../../../db/db";
interface Props {
  stopClick?: boolean
  close?: (e:any) => void
  size?: number
  attachments: ImgVideoInfo[]
}

enum DType {
  One = 1,
  Two = 2,
  Three = 3,
  Four = 4
}

type ImgModalRef = {
  openModal: (e: React.MouseEvent) => Promise<void>;
};

const ImageShow = (props: Props): JSX.Element => {
  const imgModalRef = useRef<ImgModalRef>(null);

  const ImgModal = React.forwardRef((props: any, ref: React.Ref<ImgModalRef>) => {
    const { obj } = props;
    const [preview, setPreview] = useState<boolean>(false)
    const [progress, setProgress] = useState<number>(0)

    useImperativeHandle(ref, () => ({
      openModal: (e: React.MouseEvent) => openModal(e),
    }));
    useEffect(() => {
      getUs().nc.addEvent(DBBucket.DownloadProgressEvent, async (event, removeIt) => {
        if (!obj.large || event.ids.indexOf(obj.large.objectId) < 0) {
          return;
        }
        const downProgress = await DBBucket.GetProgress(GetDScan(), [obj.large.objectId])
        setProgress(downProgress)
        if (downProgress === 100) {
          removeIt()
        }
      })
    }, [])
    const openModal = async (e: React.MouseEvent) => {
      e.stopPropagation();
      setPreview(true);
      document.body.style.overflow = 'hidden'
    }
    if(!obj.large) {
      return <></>
    }
    return (preview ? <OssImage
        style={{display: 'none'}}
        objectId={obj.large.objectId} objectKey={obj.large.key}
        isScale={true}
        progress={progress}
        onCancel={() => {
          setPreview(false)
          document.body.style.overflow = 'auto'
        }}
        placeholder={
          <OssImage
            objectId={obj.thumb.objectId}
            objectKey={obj.thumb.key}
            style={{borderRadius: '8px'}}/>
        }
      ></OssImage>: <></>)
  })

  const VideoShow = (props: any) => {
    const { obj, style } = props;
    class Info {
      objectId: string = ''
      key: string = ''
      width:number = 0
      height:number = 0
      type:string = '' // 视频或者封面图格式mp4 jpg png
    }
    const videoInfo = {
      msgId: new Date().toString(),
      video: obj.original as Info,
      cover: obj.cover as Info
    }
    return (<OSSVideoView posterWidth={style?.width} posterHeight={style?.height} posterRadius={0} isSelectMore={props.stopClick} videoInfo={videoInfo} />)
  }
  const OneImg = () => {
    return (<div className={styles.imgOneWrap}>
      {props.attachments[0].type === Type.Image ? <OssImage
        onClick={(e) => imgModalRef.current?.openModal(e)}
        style={{width: '260px', height: '150px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
        objectId={props.attachments[0].thumb.objectId} objectKey={props.attachments[0].thumb.key}
      placeholder={<NoImage style={{height: '150px', width: '260px'}} />} /> : <VideoShow obj={props.attachments[0]} style={{width: '260', height: '150'}}></VideoShow>
      }
      {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
      <ImgModal obj={props.attachments[0]} ref={imgModalRef}></ImgModal>
    </div>)
  }
  const TwoImg = () => {
    const [index, setIndex] = useState<number>(0);
    return (<div className={styles.imgTwoWrap}>
      {
        props.attachments.map((item, index) => (
          <div className={styles.imgTwoItem} key={index}>
            {item.type === Type.Image ? <OssImage onClick={(e) => {setIndex(index); imgModalRef.current?.openModal(e)}}
              style={{width: '125px', height: '150px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
              objectId={item.thumb.objectId} objectKey={item.thumb.key} placeholder={<NoImage style={{width: '125px', height: '150px'}} />} /> : <VideoShow obj={item} style={{width: '125', height: '150'}}></VideoShow>}
            <div className={styles.closeWrap}>
              {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
            </div>
          </div>
        ))
      }
      <ImgModal obj={props.attachments[index]} ref={imgModalRef} stopClick={props.stopClick}></ImgModal>
    </div>)
  }

  const ThreeImg = () => {
    const [index, setIndex] = useState<number>(0);
    return (<div className={styles.imgThreeWrap}>
      <div className={styles.imgThreeItem}>
        <div className={styles.oneImg}>{props.attachments[0].type === Type.Image ?
          <OssImage
            onClick={(e) => {setIndex(0); imgModalRef.current?.openModal(e)}}
            style={{width: '125px', height: '160px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
          objectId={props.attachments[0].thumb.objectId} objectKey={props.attachments[0].thumb.key} placeholder={<NoImage style={{width: '125px', height: '160px'}} />}
          /> : <VideoShow obj={props.attachments[0]} style={{width: '125', height: '160'}}></VideoShow>
        }
        </div>
        <div className={styles.closeWrap}>
          {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
        </div>
      </div>
      <div className={styles.imgThreeRight}>
        <div className={classNames([styles.imgThreeItem, styles.imgThreeRightItem])}>
          <div className={styles.twoImg}>{props.attachments[1].type === Type.Image ?
            <OssImage onClick={(e) => {setIndex(1); imgModalRef.current?.openModal(e)}}
            style={{width: '125px', height: '75px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
            objectId={props.attachments[1].thumb.objectId} objectKey={props.attachments[1].thumb.key} placeholder={<NoImage style={{width: '125px', height: '75px'}} />} /> :
            <VideoShow obj={props.attachments[1]} style={{width: '125', height: '75'}}></VideoShow>}
          </div>
          <div className={styles.closeWrap}>
            {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
          </div>
        </div>
        <div className={classNames([styles.imgThreeItem, styles.imgThreeRightItem])}>
          <div className={styles.threeImg}>{props.attachments[2].type === Type.Image ?
            <OssImage onClick={(e) => {setIndex(2); imgModalRef.current?.openModal(e)}}
            style={{width: '125px', height: '75px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
            objectId={props.attachments[2].thumb.objectId} objectKey={props.attachments[2].thumb.key} placeholder={<NoImage style={{width: '125px', height: '75px'}} />} /> :
            <VideoShow obj={props.attachments[2]} style={{width: '125', height: '75'}}></VideoShow>}
          </div>
          <div className={styles.closeWrap}>
            {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
          </div>
        </div>
      </div>
      <ImgModal obj={props.attachments[index]} ref={imgModalRef}  stopClick={props.stopClick}></ImgModal>
    </div>)
  }
  const FourImg = () => {
    const [index, setIndex] = useState<number>(0);
    return (<div className={styles.imgFourWrap}>
      {
        props.attachments.map((item, index) => (
          <div className={styles.imgFourItem} key={index}>
            <div className={classNames([styles.articleImg, styles[`img${index + 1}`]])}>{item.type === Type.Image ?
              <OssImage onClick={(e) => {setIndex(index); imgModalRef.current?.openModal(e)}}
              style={{width: '125px', height: '75px', maxWidth: `${window.innerWidth * 0.8}px`,maxHeight: `${window.innerHeight * 0.8}px`, objectFit: 'cover'}}
              objectId={item.thumb.objectId} objectKey={item.thumb.key} placeholder={<NoImage style={{width: '125px', height: '75px'}} />} /> :
              <VideoShow obj={item} style={{width: '125', height: '75'}}></VideoShow>}
            </div>
            <div className={styles.closeWrap}>
              {props.close && <Image className={styles.close} src={deleteBtn} onClick={props.close} preview={false} />}
            </div>
          </div>
        ))
      }
      <ImgModal obj={props.attachments[index]} ref={imgModalRef}  stopClick={props.stopClick}></ImgModal>
    </div>)
  }


  const buildImgVideo = (attachments: ImgVideoInfo[]) => {
    const map:Map<DType, () => JSX.Element> = new Map([
      [DType.One, () => <OneImg />],
      [DType.Two, () => <TwoImg />],
      [DType.Three, () => <ThreeImg />],
      [DType.Four, () => <FourImg />]
    ])
    if (!map.has(attachments.length)) {
      return <></>;
    }
    return map.get(attachments.length)!();
  }

  return (<>
    {
      buildImgVideo(props.attachments)
    }
  </>);
};
export default ImageShow;
