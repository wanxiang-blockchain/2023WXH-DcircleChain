import React, { Fragment } from "react";
import styles from "./index.module.scss";
import {Image} from "antd";
import zip from './image/zip.png'
import doc from './image/doc.png'
import ppt from './image/ppt.png'
import xls from './image/xls.png'
import mp3 from './image/mp3.png'
import txt from './image/txt.png'
import pdf from './image/pdf.png'
import apk from './image/apk.png'
import avi from './image/avi.png'
import z7 from './image/7z.png'
import {MsgContent, MsgFileContent} from "../../helper/Message";
import {BusHelper} from "../../helper/BusHelper";
import {getIsDcircleEnv, getIsWxEnv} from "../../helper/getRunningEnv";
import {Async} from "../../Async";
import {getUs} from "../../DIDBrowser";
import {DBGroup} from "../../db/DBGroup";
import copy from "copy-to-clipboard";
import {getHttp, getNavigate} from "../../helper/handleUrl";
import {AppScheme} from "../../config";

export class FileInfo {
  name:string = '';
  size: string = '';
  objectId: string = '';
  key: string = '';
}
interface  Props {
  content: MsgContent
  isSelectMore?: boolean
  stopClick?:boolean
  address: string
}
interface  State {
  date:MsgFileContent,
  decrypted:Uint8Array
  process:number
  timers: NodeJS.Timer | null
}

class MsgFileCell extends React.Component<Props, State> {
  constructor(props:Props | Readonly<Props>) {
    super(props);
    this.state = {
      date: new MsgFileContent(),
      decrypted: new Uint8Array(),
      process: 0,
      timers: null
    }
  }
  async componentDidMount() {
    const date = this.props.content as MsgFileContent;
    this.setState({date})
  }
  handleSize(size: number):string {
    if (size < 1024) {
      return size + " B";
    }
    if (size < 1024 * 1024) {
      return (size / 1024).toFixed(2) + " KB";
    }
    return (size / (1024 * 1024)).toFixed(2) + " MB";
  }

  openFile() {
    const blob = new Blob([this.state.decrypted], { type: `application/${this.state.date.suffix}` });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${this.state.date.name}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
  async openDcirlce() {
    if (getIsDcircleEnv()) {
      await BusHelper.HandleFileNcApp(this.props.address, this.state.date.msgId, this.state.date.objectId)
      return
    }
    this.toDownload();
  }
  toDownload() {
    let that = this;
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
        clearTimeout(that.state.timers as NodeJS.Timer);
      }
    });

    const url = getHttp();
    let link = document.createElement('a');
    link.href = `${AppScheme}/app?url=${url}`;
    link.click()

    let timers = setTimeout(() => {
      const route = getNavigate()
      route && route(`/download`, {state: {click: 'notAllow'}});
    }, 3000)
    this.setState({ timers: timers })
  }
  render() {
    const date = this.state.date
    const message:FileInfo = {
      objectId: date.objectId,
      name: date.name,
      key: date.key,
      size: this.handleSize(date.size)
    };
    const buildFileIcon = () => {
      const fileMap = new Map([
        ['doc', doc],
        ['docx', doc],
        ['xls', xls],
        ['xlsx', xls],
        ['ppt', ppt],
        ['pptx', ppt],
        ['pdf', pdf],
        ['zip', zip],
        ['apk', apk],
        ['avi', avi],
        ['7z', z7],
        ['mp3', mp3],
        ['txt', txt]
      ]);
      const type = message.name.split('.')[1];
      if (!fileMap.has(type)) return <Image src={fileMap.get('7z')} style={{width: '100%', height: '100%'}} preview={false} />;
      return <Image src={fileMap.get(type)} style={{width: '100%', height: '100%'}} preview={false} />
    }


    return (
      <Fragment>
        <div className={styles.file_wrap}>
          <div className={styles.message}>
            <div className={styles.messageFile} onClick={this.openDcirlce.bind(this)}>
              <div className={styles.icon}>
                {buildFileIcon()}
              </div>
              <div style={{flex: 1}}>
                <span className={styles.name}>{message.name}</span>
                <div className={styles.doc_info}>
                  <div>{message.size}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Fragment>
    )
  }
}

export default React.memo(MsgFileCell);
