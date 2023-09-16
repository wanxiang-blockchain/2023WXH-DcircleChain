import React, { useState, useEffect, useRef } from 'react';
import styles from './index.module.scss'
import playIcon from '../../image/player.png'
import pauseIcon from '../../image/pauseIcon.png'
import {VoiceInfo} from "../../MsgVoiceCell";
import {resetVoices, setVoiceControlPlayOrPause} from "../../utils/voice";
import {getIsDcircleEnv, getIsWxEnv} from "../../../../helper/getRunningEnv";
import {Async} from "../../../../Async";
import {getUs} from "../../../../DIDBrowser";
import {DBGroup} from "../../../../db/DBGroup";
import copy from "copy-to-clipboard";
import {getHttp, getNavigate} from "../../../../helper/handleUrl";
import {AppScheme} from "../../../../config";
import {BusHelper} from "../../../../helper/BusHelper";
interface Props{
  voiceInfo: VoiceInfo;
  msgId:string;
  stopClick?: boolean
  address:string
}
export function AudioPlayer(props: Props) {
  const intervalId = useRef<NodeJS.Timer>()
  const progressIntervalId = useRef<NodeJS.Timer>()
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState<number|string>('');
  const audioRef = useRef<HTMLAudioElement>(null);
  const animationRef = useRef(null);
  const [allTimeLen, setAllTimeLen] = useState('');
  const [voiceUrl, setVoiceUrl] = useState<string>('');
  const [heights, setHeights] = useState<{height: number, background:string, opacity: string}[]>([])
  const source = useRef< MediaElementAudioSourceNode|null>(null);
  const timers = useRef<NodeJS.Timer>()
  const buildHeights = () => {
    const h6 = { height: 6, background:'#6f9641',opacity: '0.3'}
    const h12 = { height: 12, background:'#6f9641',opacity: '0.3'}
    const h20 = { height: 20, background:'#6f9641',opacity: '0.3'}
    setHeights([h6,h6,h6,h6,h12,h6,h6,h12,h20,h12,h6,h6,h6,h6,h12,h6,h6,h6,h6,h12,h20,h12,h6,h6,h6,h6])
  }
  useEffect(() => {
    buildHeights();
  }, [])
  useEffect(() => {
    setDuration(showTime(props.voiceInfo.duration));
  }, [props.voiceInfo])
  useEffect(() => {
    if (!voiceUrl.length) { return }
    handlePlayPauseClick().then();
  }, [voiceUrl])

  const addZero = (val: number) => {
    return val >= 10 ? val : '0' + val;
  }
  const showTime = (duration: number) => {
    const minutes:number|string = addZero(Math.floor(duration / 60));
    const seconds:number|string = addZero(Math.floor(duration % 60));
    return `${minutes}:${seconds}`
  }
  useEffect(() => {
    const audioContext = new AudioContext();
    const analyser = audioContext.createAnalyser();
    analyser.fftSize = 512;
    (audioRef.current as unknown as HTMLElement).addEventListener('loadedmetadata', function() {
      // 获取音频时长，单位为秒
      const duration = audioRef.current?.duration;
      const timeLen = showTime(duration??0);
      setAllTimeLen(timeLen);
      setDuration(timeLen);
    });

    (audioRef.current as unknown as HTMLElement).addEventListener('canplay', () => {
      if (!source.current) {
        // @ts-ignore
        source.current = audioContext.createMediaElementSource(audioRef.current);
      }

      source.current.connect(
        analyser);
      analyser.connect(audioContext.destination);
    });
    return () => {
      // @ts-ignore
      cancelAnimationFrame(animationRef.current);
      audioContext.close();
    };
  }, []);

  const resetVoice = () => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
      setDuration(showTime(audioRef.current.duration));
      setIsPlaying(false);
      clearInterval(intervalId.current)
      clearInterval(progressIntervalId.current)
      buildHeights();
    }
  }

  const handlePlayPauseClick = async () => {
    if (props.stopClick) return;
    await resetVoices(props.msgId);
    setVoiceControlPlayOrPause(props.msgId, resetVoice)
    if (!voiceUrl.length) {
      return;
    }

    if(!audioRef.current) return;
    if (intervalId) {
      clearInterval(intervalId.current)
    }
    if(progressIntervalId) {
      clearInterval(progressIntervalId.current)
    }
    const timer1 =  setInterval(() => {
      const currentTime = audioRef.current?.currentTime??0;
      const endTime = audioRef.current?.duration??0;
      const per = currentTime / endTime; // 计算已经播放的百分比
      if (per === 0) {
        clearInterval(progressIntervalId.current)
        return;
      }
      const heightProgress = Math.round(heights.length * per)
      let s = [...JSON.parse(JSON.stringify(heights))]
      for(let i = 0; i < heightProgress; i++) {
        let a = s[i];
        a.background = '#000';
      }
      setHeights(s);
    }, 100)
    progressIntervalId.current = timer1

    // 每隔一秒更新一次倒计时
    const timer = setInterval(() => {
      const currentTime = audioRef.current?.currentTime??0;
      const endTime = audioRef.current?.duration??0;
      // 计算倒计时剩余时间
      const remainingTime = endTime - currentTime;

      // 如果剩余时间小于等于 0，清除计时器并重置倒计时显示
      if (currentTime === endTime) {
        clearInterval(timer);
        clearInterval(timer1);
        buildHeights();
        setDuration(showTime(endTime));
        setIsPlaying(false);
      } else {
        const result = showTime(remainingTime);
        setDuration(result);
      }
    }, 900);
    intervalId.current = timer;
    if (isPlaying) {
      audioRef.current.pause();
      setIsPlaying(false);
      clearInterval(intervalId.current)
      clearInterval(progressIntervalId.current)
      return;
    }
    audioRef.current.play();
    setIsPlaying(true);
  };

  const renderPlayStyle = () => {
    return (
      <div style={{display: 'flex',alignItems: 'center'}}>
        {
          heights.map((item, index) => {
            return (<div key={index} className={styles.pHeight} style={item}></div>)
          })
        }
      </div>
    );
  }

  const openDcirlce = async () => {
    // 微信中打开，直接弹窗引导在浏览器中打开
    if (getIsWxEnv()) {
      Async(async () => {
        await getUs().nc.post(new DBGroup.ModalChangedEvent(['true']))
      })
      return;
    }

    if (getIsDcircleEnv()) {
      await BusHelper.HandleVoiceNcApp(props.address, props.msgId)
      return
    }
    toDownload();
  }
  const toDownload = () => {
    copy(getHttp())

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
      const route = getNavigate()
      route && route(`/download`, {state: {click: 'notAllow'}});
    }, 3000)
  }

  return (
    <div className={styles.audioWrap}>
      <div className={styles.playIcon} onClick={openDcirlce}>
        <img className={styles.playIcon} src={!isPlaying ? playIcon : pauseIcon} alt=""/>
      </div>
      <div className={styles.duration}>{duration}</div>
      <audio ref={audioRef} src={voiceUrl} />
      <div className={styles.canvasWrap}>
        {renderPlayStyle()}
      </div>
    </div>
  );
}
