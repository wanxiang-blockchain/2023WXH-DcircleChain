import {  Modal  } from "antd";
import React from "react";
import styles from "./index.module.scss";
import player from "../images/player.png";
import {VideoInfo} from "../pages/cell/MsgVideoCell";
import {LRUCache} from "../helper/LRUCache";
import NoImage from "../pages/component/NoImage";
import {OssImage} from "./OssImage";
import {getUs} from "../DIDBrowser";
import {DBBucket} from "../db/DBBucket";
import {GetDScan} from "../db/db";
import {Downloader} from "./FileNet";
import {Aes} from "../helper/Aes";
import {getLocalFile} from "./LocalFile";
import {getBase64Image} from "../helper/getBase64Image";
import Progress from "../pages/component/Progress";

interface Props {
  videoInfo: VideoInfo
  setShowMenu?: (preview:boolean) => void
  isSelectMore?: boolean
  posterWidth?: string
  posterHeight?: string
  posterRadius?: number
}

interface State {
  videoUrl:string,
  poster:string,
  modalOpen:boolean,
  startDown:boolean,
  process:number,
  videoData: Uint8Array,
  loadFromServerCount: number,
  loadCoverFromServerCount: number,
  closable: boolean
}

const caches:LRUCache<string, string> = new LRUCache(100);

export default class OSSVideoView extends React.Component<Props, State> {
  constructor(props:Props | Readonly<Props>) {
    super(props);
    this.state = {
      videoUrl: '',
      poster: '',
      modalOpen: false,
      startDown: false,
      process: 0,
      videoData: new Uint8Array(),
      loadFromServerCount: 0,
      loadCoverFromServerCount: 0,
      closable: false
    };
  }

  async componentDidMount() {
    getUs().nc.addEvent(DBBucket.DownloadProgressEvent, async (event, removeIt) => {
      const {cover, video} = this.props.videoInfo
      if (event.ids.indexOf(video.objectId)>=0) {
        const progress = await DBBucket.GetProgress(GetDScan(), [video.objectId])
        this.setState({process: progress})
        if (progress === 100) {
          this.setState({startDown: false}, () => {
            removeIt();
          })
        }
      }
      if (event.ids.indexOf(cover.objectId)>=0) {
        const progress = await DBBucket.GetProgress(GetDScan(), [cover.objectId])
        if (progress === 100) {
          if (this.state.loadCoverFromServerCount > 2) return;
          await this.loadCoverFromDB(cover.objectId, cover.key, cover.type)
        }
      }
    })
    await this.loadCoverFromDB(this.props.videoInfo.cover.objectId, this.props.videoInfo.cover.key, this.props.videoInfo.cover.type)
  }

  async componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
    if (this.props.videoInfo.cover.objectId !== prevProps.videoInfo.cover.objectId) {
      if (!this.props.videoInfo.cover.objectId) return
      if (this.state.poster.length > 0) return;
      await this.loadCoverFromDB(this.props.videoInfo.cover.objectId, this.props.videoInfo.cover.key, this.props.videoInfo.cover.type)
    }
  }

  async loadCoverFromDB(objectId:string, key:string, type: string) {
    if (this.state.poster.length) return;
    const [content, err1] = await getLocalFile().read(objectId);
    this.setState({loadCoverFromServerCount: this.state.loadCoverFromServerCount + 1})
    if (err1) {
      if (this.state.loadCoverFromServerCount <= 2) {
        await this.loadCoverFromServer(objectId);
        return;
      };

      return ;
    }
    const aes = new Aes(key);
    let decrypted = aes.decrypt(content);
    const imgUrl = getBase64Image(type, decrypted);
    this.setState({ poster: imgUrl })
  }

  async loadCoverFromServer(objectId:string) {
    Downloader.getInstance().addRequest(objectId);
  }

  async loadVideoFromDB(objectId:string, key:string, autoplay: boolean = false) {
    const value = caches.get(objectId)
    if (value) {
      this.setState({ videoUrl: value }, () => { this.openVideo(); })
      return ;
    }
    const [content, err1] = await getLocalFile().read(objectId);
    if (err1) {
      console.warn("cannot find file fromDB", err1)
      this.setState({startDown: true, loadFromServerCount: this.state.loadFromServerCount + 1}, async () => {
        if (this.state.loadFromServerCount <= 2) {
          await this.loadVideoFromServer(objectId);
          return;
        }
      })
      return ;
    }
    this.openVideo().then();
    const aes = new Aes(key);
    const decrypted = aes.decrypt(content);
    const videoBlob = new Blob([decrypted], { type: `video/${this.props.videoInfo.video.type}` });
    const videoUrl = URL.createObjectURL(videoBlob);
    caches.set(objectId, videoUrl)
    this.setState({
      videoData: decrypted,
      videoUrl: videoUrl
    })
  }

  async loadVideoFromServer(objectId:string) {
    Downloader.getInstance().addRequest(objectId);
  }

  async openPlayer() {
    if (this.state.startDown) return;
    const {objectId, key} = this.props.videoInfo.video;
    if (!objectId.length || !key.length) { return }
    // 如果视频地址不存在
    if (!this.state.videoUrl.length) {
      await this.loadVideoFromDB(objectId, key);
     return;
    }
    await this.openVideo();
  }
  async openVideo() {
    this.props.setShowMenu && this.props.setShowMenu(true)
    this.setState({modalOpen: true});
    this.setState({closable: true});
  }
  onCancel() {
    this.props.setShowMenu && this.props.setShowMenu(false)
    this.setState({
      modalOpen: false,
      closable: false
    }, () => {
      let video = document.getElementById(`${this.props.videoInfo.msgId}video`) as HTMLVideoElement;
      if (video) {
        video.pause();
        video.currentTime = 0;
      }
    })
  }

  render() {
    let viewportWidth = window.innerWidth;
    let viewportHeight = window.innerHeight;
    let {width, height} = this.props.videoInfo.video;
    if (width === 0) width = 450
    if (height === 0) height = 850
    if (width > viewportWidth) width = viewportWidth
    if (height > viewportHeight) height = viewportHeight
    let _posterWidth = this.props.posterWidth ? this.props.posterWidth : this.props.videoInfo.cover.width
    let _posterHeight = this.props.posterWidth ? this.props.posterHeight : this.props.videoInfo.cover.height
    return (
      <div className={styles.playerBox} style={{width: _posterWidth + 'px',height: _posterHeight + 'px', borderRadius: this.props.posterRadius}}>
        <OssImage
          objectId={this.props.videoInfo.cover.objectId}
          objectKey={this.props.videoInfo.cover.key}
          style={{height: _posterHeight + 'px', width: _posterWidth + 'px', borderRadius: '8px'}}
          placeholder={<NoImage style={{height: _posterHeight, width: _posterWidth, objectFit: 'contain'}} />}/>
        <div className={styles.player} onClick={this.openPlayer.bind(this)}>
          {(this.state.process === 0 || this.state.process === 100) && <img src={player} alt="" />}
          <Progress percent={this.state.process} size={40} />
        </div>
        <Modal
          centered
          className={styles.model_wrp}
          getContainer={false}
          width={'auto'}
          footer={null}
          open={this.state.modalOpen}
          onCancel={this.onCancel.bind(this)}
          closable={this.state.closable}
          afterOpenChange={open => {
              document.body.style.overflow = open ? 'hidden' : 'auto'
            }
          }
        >
          <div className={styles.video_wrap} id={this.props.videoInfo.video.objectId} style={{width: `${width * 0.8}px`, height: `${height * 0.8}px`}}>
            <video id={`${this.props.videoInfo.msgId}video`} src={this.state.videoUrl} style={{width: '100%', height: '100%'}} controls={true} autoPlay={true}></video>
          </div>
        </Modal>
      </div>
    );
  }
}
