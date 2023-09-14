import styles from './index.module.scss';
import classnames from 'classnames';
import { useEffect, useState } from 'react';
import {Async} from "../../../../Async";
import {DIDRole} from "../../../../api/DIDRole";
import {DBDIDArticleRole} from "../../../../db/DBDIDArticleRole";
import {GetDScan} from "../../../../db/db";
import {getUs} from "../../../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;
interface Props {
  uid:string
  uidSource?:'group'|'';
  children: JSX.Element;
  type?: string;
  height?: string;
}

const AvatarBorder = ({ children, type, height, uid, uidSource='' }: Props): JSX.Element => {
  const [createNum, setCreateNum] = useState<number>(0);
  const [groupNum, setGroupNum] = useState<number>(0);
  const [transferNum, setTransferNum] = useState<number>(0);
  const [consumerNum, setConsumerNum] = useState<number>(0);

    useEffect(() => {
      Async(async () => {
        getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
          if (!event.ids.includes(uid)) return
          if (event.source != "group_index") return;
          await loadDB();
        })
        await loadDB();
      })
    }, [])

  const loadDB = async () => {
    const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(uid, DIDRole.SingleGroupStat));
    if (err) {
      return;
    }
    if (uidSource.length > 0 && uidSource === 'group') {
      setCreateNum(ret.cNums)
      setGroupNum((ret.nNums))
      setTransferNum(ret.cTimes)
      return;
    }
    setCreateNum(ret.cTimes)
    setGroupNum((ret.gNums))
    setTransferNum(ret.tNums)
    setConsumerNum(ret.cNums)
  }


  const getColorByData = (x: number, type?: string) => {
    const map = [
      { x: 1000.0, k_r: -0.044, b_r: 277.0, k_g: -0.23, b_g: 269.0, k_b: 0.078, b_b: -39.0 },
      { x: 500.0, k_r: -0.044, b_r: 277.0, k_g: -0.23, b_g: 269.0, k_b: 0.078, b_b: -39.0 },
      { x: 200.0, k_r: 0.373, b_r: 68.333, k_g: 0.263, b_g: 22.333, k_b: -0.793, b_b: 396.667 },
      { x: 50.0, k_r: 0.567, b_r: 29.667, k_g: -0.393, b_g: 153.667, k_b: -0.113, b_b: 260.667 },
      { x: 10.0, k_r: -0.65, b_r: 90.5, k_g: -2.5, b_g: 259.0, k_b: 1.025, b_b: 203.75 },
      { x: 1.0, k_r: -5.556, b_r: 139.556, k_g: 0.444, b_g: 229.556, k_b: 15.0, b_b: 64.0 },
      { x: 0.0, k_r: 242.0, b_r: 0.0, k_g: 243.0, b_g: 0.0, k_b: 245.0, b_b: 0.0 }
    ];

    let curP = map[map.length - 1];
    for (const p of map) {
      if (x >= p.x) {
        curP = p;
        break;
      }
    }

    const r = (curP.k_r * x + curP.b_r);
    const g = (curP.k_g * x + curP.b_g);
    const b = (curP.k_b * x + curP.b_b);
    return `rgba(${r}, ${g}, ${b}, ${type === 'opacity' ? 0.2 : 1})`;
  }
  const RangeMap = new Map([
    ['1', `linear-gradient(135deg, rgba(255, 255, 255, 0), ${getColorByData(groupNum)}`],
    ['2', `linear-gradient(227deg, rgba(255, 255, 255, 0), ${getColorByData(createNum)}`],
    ['3', `linear-gradient(225deg, rgba(255, 255, 255, 0), ${getColorByData(transferNum)})`],
    ['4', `linear-gradient(135deg, ${getColorByData(consumerNum)}, rgba(255, 255, 255, 0))`],
    ['5', `linear-gradient(135deg, ${getColorByData(50)}, rgba(255, 255, 255, 0))`],
    ['6', `linear-gradient(135deg, ${getColorByData(60)}, rgba(255, 255, 255, 0))`]
  ])
  const avatarMap = new Map([
    ['border30', '30px'],
    ['border64', '64px'],
    ['border110', '110px'],
  ])
  const leftMap = new Map([
    ['border30', styles.avatarBorderLeft30],
    ['border64', styles.avatarBorderLeft64],
    ['border110', styles.avatarBorderLeft110]
  ])
  const topMap = new Map([
    ['border30', styles.avatarBorderTop30],
    ['border64', styles.avatarBorderTop64],
    ['border110', styles.avatarBorderTop110]
  ])
  const rightMap = new Map([
    ['border30', styles.avatarBorderRight30],
    ['border64', styles.avatarBorderRight64],
    ['border110', styles.avatarBorderRight110]
  ])
  const rightBottomMap = new Map([
    ['border30', styles.avatarBorderRightBottom30],
    ['border64', styles.avatarBorderRightBottom64],
    ['border110', styles.avatarBorderRightBottom110]
  ])
  const bottomMap = new Map([
    ['border30', styles.avatarBorderBottom30],
    ['border64', styles.avatarBorderBottom64],
    ['border110', styles.avatarBorderBottom110]
  ])
  const leftBottomMap = new Map([
    ['border30', styles.avatarBorderLeftBottom30],
    ['border64', styles.avatarBorderLeftBottom64],
    ['border110', styles.avatarBorderLeftBottom110]
  ])
  if(type === undefined) {type = ''}
  const avatarSize = avatarMap.has(type) ? avatarMap.get(type) : '';
  const leftSize = leftMap.has(type) ? leftMap.get(type) : '';
  const topSize = topMap.has(type) ? topMap.get(type) : '';
  const rightSize = rightMap.has(type) ? rightMap.get(type) : '';
  const rightBottomSize = rightBottomMap.has(type) ? rightBottomMap.get(type) : '';
  const bottomSize = bottomMap.has(type) ? bottomMap.get(type) : '';
  const leftBottomSize = leftBottomMap.has(type) ? leftBottomMap.get(type) : '';
  return (
    <div className={styles.avatarBorderWrap} style={{width: avatarSize || '38px', height: height || avatarSize || '34px'}}>
      {children}
      <div className={classnames([styles.avatarBorder1, leftSize])} style={{background: groupNum === 0 ? '#F1F1F1' : RangeMap.get('1')}}></div>
      <div className={classnames([styles.avatarBorder2, topSize])} style={{background: createNum === 0 ? '#F1F1F1' : RangeMap.get('2')}}></div>
      <div className={classnames([styles.avatarBorder3, rightSize])} style={{background: transferNum === 0 ? '#F1F1F1' : RangeMap.get('3')}}></div>
      <div className={classnames([styles.avatarBorder4, rightBottomSize])} style={{background: consumerNum === 0 ? '#F1F1F1' : RangeMap.get('4')}}></div>
      <div className={classnames([styles.avatarBorder5, bottomSize])} style={{background: true ? '#F1F1F1' : RangeMap.get('5')}}></div>
      <div className={classnames([styles.avatarBorder6, leftBottomSize])} style={{background: true ? '#F1F1F1' : RangeMap.get('6')}}></div>
    </div>
  );
};

export default AvatarBorder;
