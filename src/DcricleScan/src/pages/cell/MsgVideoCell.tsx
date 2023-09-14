import React, {Fragment} from "react";
import styles from "./index.module.scss";
import OSSVideoView from "../../oss/OSSVideoView";
import {MsgContent, MsgVideoContent, Type} from "../../helper/Message";
interface Props {
  content: MsgContent;
  isSelectMore?: boolean;
}
class Info {
  objectId: string = ''
  key: string = ''
  width:number = 0
  height:number = 0
  type:string = '' // 视频或者封面图格式mp4 jpg png
}
export class VideoInfo {
  msgId: string = ''
  video: Info = new Info()
  cover: Info = new Info()
}
interface  State {
  videoInfo: VideoInfo;
  data: MsgVideoContent;
  preview: boolean
}
class MsgVideoCell extends React.Component<Props, State> {
  constructor(props:Props | Readonly<Props>) {
    super(props);
    this.state = {
      videoInfo: new VideoInfo(),
      data: new MsgVideoContent(),
      preview: false
    };
  }
  componentDidMount() {
    if (this.props.content.type !== Type.Video) {
      throw new Error(`MsgImageCell's type(${this.props.content.type}) invalid`)
    }
    let cont = this.props.content as MsgVideoContent;
    const _v = new VideoInfo();
    _v.msgId = cont.msgId
    _v.video.key = cont.original.key
    _v.video.objectId = cont.original.objectId
    _v.video.width = cont.original.width
    _v.video.height = cont.original.height
    _v.video.type = cont.original.type
    _v.cover.key = cont.cover.key
    _v.cover.objectId = cont.cover.objectId
    _v.cover.width = cont.cover.width
    _v.cover.height = cont.cover.height
    _v.cover.type = cont.cover.type
    this.setState({
      data: cont,
      videoInfo: _v
    })
  }
  setShowMenu(preview:boolean) {
    this.setState({preview: preview})
  }
  render () {
    return (
      <Fragment>
        <div className={styles.videoMesRCtx}>
            <OSSVideoView videoInfo={this.state.videoInfo} setShowMenu={this.setShowMenu.bind(this)}/>
        </div>
      </Fragment>
    )
  }
}

export default React.memo(MsgVideoCell);
