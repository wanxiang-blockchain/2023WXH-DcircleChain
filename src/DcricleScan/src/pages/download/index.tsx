import styles from "./index.module.scss"
import React, {useEffect, useRef, useState} from "react";
import {getHttp} from "../../helper/handleUrl";
import logo from "./download_logo.png"
import text_pic from "./download_text.png"
import android from "./android.png"
import googlePlay from "./GooglePlay.png"
import appStore from "./appStore.png"
import classNames from "classnames";
import toBrower from "./toBrower.png";
import {Modal} from "antd";
import copy from "copy-to-clipboard";
import {GetLatestReleasedVersion, GetLatestReleasedVersionResponse} from "../../api/GetLatestReleasedVersion";
import {Async} from "../../Async";
import {AppScheme} from "../../config";
import {getIsWxEnv} from "../../helper/getRunningEnv";
import classnames from "classnames";
export function Download() {
  const [downloadInfo, setDownloadInfo] = useState(new GetLatestReleasedVersionResponse());
  const [showModel, setShowModel] = useState(false);
  useEffect(() => {
    if (getIsWxEnv()) {
      setShowModel(true)
      return
    }
    Async(async () => {
      !getIsWxEnv() && getLatestReleasedVersion().then()
    })
    let dom = document.getElementById('tipsDownload');
    if (!dom) return;
    dom.style.display = "none"
    if (true) {
      window.scrollTo({ top: 0, behavior: "smooth"  });
    }
  }, [])
  const getLatestReleasedVersion = async () => {
    let [ret, err] = await GetLatestReleasedVersion();
    if (err) {
      console.error('获取最新下载地址失败')
      // 下载失败会不断重试，直到下载成功 - 不处理重试次数
      getLatestReleasedVersion().then()
      return;
    }
    setDownloadInfo(ret)
  }
  const handleClick = () => {
    const url = getHttp();
    window.open(`${AppScheme}/app?url=${url}`, '_self')
  }

  const openAndroid = async () => {
    if (getIsWxEnv()) {
      setShowModel(true)
      return
    }
    copy(getHttp())
    if (downloadInfo.data.latestVersion.downLoadUrl.length <= 0) return;
    window.location.href = downloadInfo.data.latestVersion.downLoadUrl;
  }
  const openGooglePlay = () => {
    if (getIsWxEnv()) {
      setShowModel(true)
      return
    }
    copy(getHttp())
    window.location.href = 'https://play.google.com/store/apps/details?id=com.yhtech.dcircle'
  }

  return (
    <div className={classnames([styles.main, styles.download_main])}>
      <Modal
        centered
        width={'100%'}
        closeIcon={false}
        getContainer={false}
        keyboard={false}
        closable={false}
        open={showModel}
        footer={null}
      >
        <div className={styles.download_wrap_wx_zhiying}>
          <img src={toBrower} />
        </div>
      </Modal>
      <div className={styles.icon}>
        <img className={styles.img} src={logo} alt=""/>
      </div>
      <div className={styles.text_pic}>
        <img className={styles.img} src={text_pic} alt=""/>
      </div>
      <div
        className={classNames([styles.btn, styles.android_btn])}
        onClick={openAndroid}
      >
        <img className={styles.android} src={android} alt=""/>
        <span className={styles.text}>Android APK</span>
      </div>
      <div className={classNames([styles.btn, styles.google_btn])} onClick={openGooglePlay}>
        <img className={styles.android} src={googlePlay} alt=""/>
        <span className={styles.text}>Google Play</span>
      </div>
      <div className={classNames([styles.btn, styles.appStore_btn])}>
        <img className={styles.android} src={appStore} alt=""/>
        <span className={styles.text}>Comming Soon </span>
      </div>
      <div className={styles.bg2}>
      </div>
    </div>
  );
}
