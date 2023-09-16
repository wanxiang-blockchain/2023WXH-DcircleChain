import styles from './index.module.scss';
import React from 'react';
import {MsgContent, MsgImageContent, Type} from "../../helper/Message";
import {getUs} from "../../DIDBrowser";
import {DBBucket} from "../../db/DBBucket";
import {GetDScan} from "../../db/db";
import {OssImage} from "../../oss/OssImage";
import NoImage from '../component/NoImage';
import Progress from "../component/Progress";
import {InitRustLib} from "../../InitRustLib";
interface Props {
  content: MsgContent;
}

interface  State {
  chatId: string;
  data: MsgImageContent;
  preview?:boolean;
  progress:number;
  progress1:number;
}
class MsgImageCell extends React.Component<Props, State> {
  constructor(props:Props | Readonly<Props>) {
    super(props);
    this.state = {
      chatId: '',
      data: new MsgImageContent(),
      progress: 0,
      progress1: 0
    };
  }
  public clsName = this.props.content.type === 1 ? styles.migMesRCtx : styles.migMesLCtx;

  async componentDidMount() {
    await InitRustLib();
    if (this.props.content.type !== Type.Image) {
      throw new Error(`MsgImageCell's type(${this.props.content.type}) invalid`)
    }

    const data = this.props.content as MsgImageContent;
    this.setState({data}, () => {
      getUs().nc.addEvent(DBBucket.DownloadProgressEvent, async (event, removeIt) => {
        if (event.ids.indexOf(this.state.data.large.objectId) < 0) {
          return;
        }
        const downProgress = await DBBucket.GetProgress(GetDScan(), [this.state.data.large.objectId])
        this.setState({progress1: downProgress}, async () => {
          if (this.state.progress1 === 100) {
            removeIt()
          }
        })
      })
      getUs().nc.addEvent(DBBucket.UploadProgressEvent, async (event, removeIt) => {
        if (event.ids.indexOf(this.state.data.thumb.objectId) < 0) {
          return;
        }
        const upProgress = await DBBucket.GetProgress(GetDScan(), [this.state.data.thumb.objectId])
        this.setState({progress: upProgress}, () => {
          if (this.state.progress === 100) {
            removeIt()
          }
        })
      })
    })
  }

  async onModalOpen(){
    this.setState({preview:true});
    document.body.style.overflow = 'hidden'
  }
  render () {
    const content = this.props.content as MsgImageContent;
    return <>
      {this.state.preview && <OssImage
          style={{display: 'none'}}
          objectId={content.large.objectId} objectKey={content.large.key}
          isScale={true}
          progress={this.state.progress1}
          onCancel={() => this.setState({preview: false})}
          placeholder={
            <OssImage
              objectId={content.thumb.objectId}
              objectKey={content.thumb.key}
              style={{borderRadius: '8px'}}/>
          }
      ></OssImage>}
      <div className={this.clsName}>
        <div className={styles.imgWrap} style={{height: this.state.data.thumb.height, width: '100%'}}>
          { <Progress percent={this.state.progress} size={40} />}
          <OssImage onClick={this.onModalOpen.bind(this)}
            objectId={content.thumb.objectId}
            objectKey={content.thumb.key}
            style={{height: this.state.data.thumb.height, borderRadius: '8px'}}
            placeholder={<NoImage style={{height: this.state.data.thumb.height, width: this.state.data.thumb.width, objectFit: 'contain'}} />}/>
        </div>
      </div>
    </>
  }
}

export default React.memo(MsgImageCell);
