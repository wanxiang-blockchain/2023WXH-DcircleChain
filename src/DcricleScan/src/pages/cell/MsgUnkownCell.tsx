import classNames from 'classnames';
import styles from './index.module.scss';
import {memo} from "react";
import {MsgContent} from "../../helper/Message";

const MsgUnkownCell = function (content:MsgContent):JSX.Element {
  return <>
      <div  className={classNames([styles.unkownMessage])}>
        <span className={styles.time}>该消息暂不支持</span>
      </div>
    </>
}

export default memo(MsgUnkownCell);
