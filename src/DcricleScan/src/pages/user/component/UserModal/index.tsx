import {Modal} from 'antd';
import styles from './index.module.scss'
import icon3 from "../../../group/image/icon3.png";
import icon4 from "../../../group/image/icon4.png";
import {FormattedMessage} from "react-intl";
import DidData from "../../../component/didData";
import SpreadDidData from "../../../component/spreadDidData";
import React from "react";
import {UserTabType} from "../../index";
interface DidObj {
    title: string;
    tips1: string;
    tips2: string;
}
interface Props {
    open: boolean
    type: string
    close: Function
    didData: DidItem[]
    didObj: DidObj
}

interface DidItem {
    title: string;
    value: number;
}

const UserModal = ({ open = false, type = UserTabType.Group, close = () => {}, didData = [], didObj = {} as DidObj }: Props): JSX.Element => {
  return (
      <Modal
          centered
          closable={false}
          maskClosable={true}
          open={open}
          onCancel={() => close(false)}
          footer={null}>
          <div className={styles.modalWrap}>
              <div className={styles.modalMainWrap}>
                  <div className={styles.titleWrap}>
                      <img className={styles.icon} src={type === UserTabType.Spread ? icon3 : icon4} alt=""/>
                      <div className={styles.modalTitle}>
                          {didObj.title}
                      </div>
                  </div>
                  <div className={styles.tips1}>
                      {didObj.tips1}
                  </div>
                  <div className={styles.tips2}>
                      {didObj.tips2}
                  </div>
                  {
                      type !== UserTabType.Spread ? <DidData data={didData}></DidData> : <SpreadDidData data1={didData.slice(0, 4)} data2={didData.slice(4, 7)}></SpreadDidData>
                  }
              </div>
              <div className={styles.known} onClick={() => close(false)}><FormattedMessage id="picture_know" /></div>
          </div>
      </Modal>
  );
};

export default UserModal;
