import React, {Fragment, useEffect, useState, memo} from 'react';
import styles from "./index.module.scss";
import { Async } from '../../Async';
import {MsgContent, MsgTextContent, Type} from "../../helper/Message";

const MsgTextCell = function (content:MsgContent):JSX.Element {
  const [data, setData] = useState<MsgTextContent>(new MsgTextContent());
  const [formatText, setFormatText] = useState<string|TrustedHTML>('');
  useEffect(() => {
    Async(async() => {
      if (content.type !== Type.Text) {
        throw new Error(`MsgTextCell's type(${content.type}) invalid`)
      }
      setData(content as MsgTextContent);
      const messageText = await (content as MsgTextContent).text;
      setFormatText(messageText);

    })
  },[content])

  function handleClick(event: any) {
    if (!event.target.id.length) return;
    const dom = document.getElementById(event.target.id);
    if (!dom) return;;
    const didAddress = dom.getAttribute('data-addr')
    if (!didAddress) return;
    const noDupId = dom.getAttribute('data-nid');
  }
  return (
    <Fragment>
      <div className={styles.msgWrap}>
        <div style={{display: formatText ? 'flex' : 'none'}} className={styles.mesRCtx}>
          <span
            className={styles.paragraphFirst}
            onClick={handleClick}
            dangerouslySetInnerHTML={{
              __html: formatText
            }}
          />
        </div>
      </div>
    </Fragment>
  )
}
export default memo(MsgTextCell)
