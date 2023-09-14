import styles from "./styles/index.module.scss";
import React from 'react';
import upJianTou from "../../group/image/upJianTou.png";
import {FormattedMessage} from "react-intl";

interface DidItem{
    title: string;
    value: number;
}
interface Props {
    data1: DidItem[]
    data2: DidItem[]
}
export default function SpreadDidData({data1, data2}: Props) {

  return (
      <>
          <div className={styles.effectiveDataWrap}>
              <span className={styles.timesTitle}><FormattedMessage id="personal_did_pob_did_effective_interactions_number" />：</span>
              <span className={styles.timesNum}>{data1[0].value + data2[1].value}</span>
          </div>
          <div className={styles.allDataWrap}>
              <div className={styles.dataWrap}>
                  {
                      data1.map((item, index) => (
                          <div className={styles.dataItem} key={item.title}>
                              <div className={styles.line}>
                                  {
                                      index === 0 ? <div className={styles.circle}></div> : <div className={styles.horizontalLine}>
                                          <div className={styles.verticalLine}></div>
                                      </div>
                                  }
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
              <div className={styles.dataWrap}>
                  {
                      data2.map((item,index) => (
                          <div className={styles.dataItem} key={item.title}>
                              <div className={styles.line}>
                                  {
                                      index === 0 ? <div className={styles.circle}></div> : <div className={styles.horizontalLine}>
                                          <div className={styles.verticalLine}></div>
                                      </div>
                                  }
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
          </div>
      </>
  )
}
