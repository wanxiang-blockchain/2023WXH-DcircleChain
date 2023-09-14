import styles from "./styles/index.module.scss";
import React from 'react';
import classNames from "classnames";
import upJianTou from "../../group/image/upJianTou.png";

interface DidItem{
    title: string;
    value: number;
}
interface Props {
  data: DidItem[]
}
export default function DidData({data}: Props) {

  return (
      <div className={styles.dataWrap}>
          <div className={classNames(styles.dataItem, styles.firstDataItem)}>
              <div className={styles.circle}></div>
              <div className={styles.timesItem}>
                  <span className={styles.timesTitle}>{data[0].title}：</span>
                  <span className={styles.timesNum}>{data[0].value}</span>
              </div>
          </div>
          {
              data.slice(1).map(item => (
                  <div className={styles.dataItem} key={item.title}>
                      <div className={styles.line}>
                          <div className={styles.horizontalLine}>
                              <div className={styles.verticalLine}></div>
                          </div>
                          <div className={styles.upJianTou}>
                              <img src={upJianTou} alt=""/>
                          </div>
                      </div>
                      <div className={styles.timesItem}>
                          <span className={styles.timesTitle}>{item.title}：</span>
                          <span className={styles.timesNum}>{item.value}</span>
                      </div>
                  </div>
              ))
          }
      </div>
  )
}
