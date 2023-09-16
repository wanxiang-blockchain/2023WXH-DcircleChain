import React, {useEffect, useRef, useState} from "react";
import {getHttp, getLocation, getNavigate} from "./helper/handleUrl";
import copy from "copy-to-clipboard";
import {AppScheme} from "./config";
import styles from "./pages/download/index.module.scss";
import toBrower from "./pages/download/toBrower.png";
import {Modal} from "antd";
import {DBGroup} from "./db/DBGroup";
import {getUs} from "./DIDBrowser";
import {getIsDcircleEnv, getIsWxEnv} from "./helper/getRunningEnv";

export function DownloadTips() {
  const [showModel, setShowModel] = useState(false);
  const timers = useRef<NodeJS.Timer>()

  useEffect(() => {
    getUs().nc.addEvent(DBGroup.ModalChangedEvent, (event) => {
      setShowModel(event.ids.includes('true'))
    })
  }, [])
  const toDownload = async () => {
    // 微信中打开，直接弹窗引导在浏览器中打开
    if (getIsWxEnv()) {
      setShowModel(getIsWxEnv())
      return;
    }

    const copyRet = copy(getHttp())
    if (!copyRet) {
      try {
        await navigator.clipboard.writeText(getHttp())
      } catch(e) {
        let textField = document.createElement("textarea");
        textField.innerText = getHttp();
        document.body.appendChild(textField);
        textField.select();
        textField.remove();
      }
    }

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
      const location = getLocation();
      if (location) {
        if (location.pathname.indexOf('download') > -1) {
          return
        }
      }
      const route = getNavigate()
      route && route(`/download`, {state: {click: 'notAllow'}});
    }, 3000)
  }
  const delDownload = () => {
    const dom = document.getElementById('tipsDownload');
    if (!dom) return;
    dom.remove()
  }
  const build = () => {
    if (getIsDcircleEnv()) {
      return <></>
    }
    return (
      <div className="public_status" id="tipsDownload">
        <Modal
          centered
          width={'100%'}
          closeIcon={false}
          getContainer={false}
          keyboard={false}
          onCancel={() => setShowModel(false)}
          closable={false}
          open={showModel}
          footer={null}
        >
          <div className="wrap_wx_zhiying">
            <img src={toBrower} />
          </div>
        </Modal>
        <div className="public_del" onClick={delDownload}>
          <img className="public_icon" src={process.env.PUBLIC_URL + '/del.png'} alt=""/>
        </div>
        <div className="public_left">
          <div className="public_icon_wrap">
            <img className="public_icon" src={process.env.PUBLIC_URL + '/dcircle_icon.png'} alt=""/>
          </div>
          <div className="public_left_wrap_right_wrap">
            <div className="public_title">Dcircle Official APP</div>
            <div className="public_title_mini">Immortalize Your Life On-Chain</div>
          </div>
        </div>
        <div className="public_right" onClick={toDownload}>Open</div>
      </div>
    );
  }

  return (
    <>
      {
        build()
      }
    </>
  )
}
