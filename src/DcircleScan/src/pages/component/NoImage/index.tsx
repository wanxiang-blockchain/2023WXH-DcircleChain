import styles from "./styles/index.module.scss";
import { Image } from 'antd';
import noImg from './styles/noImg.png';
import React, { CSSProperties } from 'react';

interface Props {
  style?:CSSProperties | undefined
}
export default function Index({style}: Props) {

  return (
    <div style={style} className={styles.noPosterWrap}>
      <Image className={styles.noPosterImg} src={noImg} preview={false}></Image>
    </div>
  )
}
