import styles from './index.module.scss';
import copyImg from "./image/copy.png";
import {useLocation, useParams} from 'react-router-dom';
import copy from "copy-to-clipboard";
import {message} from "antd";
import Cell from "../component/Cell";
import React, {useEffect, useState} from "react";
import {Async} from "../../Async";
import up from './image/up.png'
import down from './image/down.png'
import upJianTou from './image/upJianTou.png'
import ContentList from "./components/ContentList";
import {GetDIDArticleInfo, GetDIDArticleInfoRequest, GetDIDArticleInfoResponse} from "../../api/GetDIDArticleInfo";
import {GetDIDArticleLog, GetDIDArticleLogItem, GetDIDArticleLogRequest, UpdateType} from "../../api/GetDIDArticleLog";
import formatTime from "../../helper/formatTime";
import formatNum from "../../helper/formatNum";
import {useNavigate} from "react-router-dom";
import { Panel } from "../component/echarts";
import {FormattedMessage, useIntl} from "react-intl";
import {UserTabType, statisticsItem} from "../user";
import {GetUserStatByDIDRole, GetUserStatByDIDRoleRequest} from "../../api/GetUserStatByDIDRole";
import {DIDRole} from "../../api/DIDRole";
import LoadFail from "../component/loadFail";
import {handleQueryLinkAndSaveHttp, saveHttp} from "../../helper/handleUrl";
import Header from "../component/header";
import classNames from "classnames";
import {DBDIDArticleRole} from "../../db/DBDIDArticleRole";
import {GetDScan} from "../../db/db";
import {getUs} from "../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;
export function Article() {
  const navigate = useNavigate()
  const location = useLocation();
  // 再存一次，防止刷新时丢失
  const state = useLocation().state;
  state && saveHttp(state.url);

  handleQueryLinkAndSaveHttp(location.search);

  const { articleAddress, language } = useParams();
  const [address, setAddress] = useState<string>(articleAddress??"");
  const [direct, setDirect] =  useState<'up'|'down'>('down')
  const [articleInfo, setArticleInfo] = useState<GetDIDArticleInfoResponse>( new GetDIDArticleInfoResponse())
  const [logs, setLogs] = useState<GetDIDArticleLogItem[]>(new Array<GetDIDArticleLogItem>())
  const [statisticsData, setStatisticsData] = useState<statisticsItem[]>([]);
  const [dataUpdateTime, setDataUpdateTime] = useState<number>(0);
  const [requestCount, setRequestCount] = useState<number>(0);
  const { formatMessage } = useIntl();

  const copyFun = async(e: MouseEvent, add:string|number) => {
      e.stopPropagation();
    copy(add + '');
    await message.success(formatMessage({id: 'copy_successfully'}));
  };

  useEffect(() => {
    load(address);
    Async(async () => {
      getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
        if (!event.ids.includes(address)) return
        if (event.source != "article_index") return;
        await loadDIDAllRoleStatFromDB(address);
      })
      await loadDIDAllRoleStatFromDB(address);
      await loadDIDAllRoleStatFromServer(address)
    })
  }, [address, formatMessage]);

  const load = (address:string) => {
    loadArticleInfo(address).then()
    loadLog(address).then()
  }
  const loadDIDAllRoleStatFromServer = async (address:string) => {
    const request = new GetUserStatByDIDRoleRequest();
    request.address = address;
    request.role = DIDRole.ArticleStat;
    await GetUserStatByDIDRole(request, 'article_index');
  }
  const loadDIDAllRoleStatFromDB = async (address:string) => {
    const id = buildId(address, DIDRole.ArticleStat)
    const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), id);
    if (err) {
      return;
    }
    const statisticsData = [{
        title: formatMessage({id: 'consumption_times'}),
        icon: 'icon-jiaoyicishu',
        type: UserTabType.Group,
        flex: 'column',
        count: ret.cTimes
    },{
        title: formatMessage({id: 'message_consumers'}),
        icon: 'icon-xiaofeirenshu',
        type: UserTabType.Create,
        flex: 'row',
        count: ret.cNums
    },{
        title: formatMessage({id: 'communication_group'}),
        icon: 'icon-chuanboli1',
        type: UserTabType.Spread,
        flex: 'row',
        count: ret.gNums
    },{
        title: formatMessage({id: 'disseminator'}),
        icon: 'icon-chuanboli',
        type: UserTabType.Consume,
        flex: 'column',
        count: ret.tNums
    },{
        type: UserTabType.i,
        flex: 'column',
        count: 1
    }]
    setStatisticsData(statisticsData);
    setDataUpdateTime(ret.dataUpdateTime);
    if (err) {
        const count = requestCount + 1;
        setRequestCount(count);
        message.error(`Error: GetDIDArticleInfo is fail: ${err}`)
        return;
    }
  }

  const loadArticleInfo = async (address:string, version: number = 0) => {
    const request = new GetDIDArticleInfoRequest();
    request.address = address;
    request.version = version;
    const [ret, err] = await GetDIDArticleInfo(request)
    if (err) {
      const count = requestCount + 1;
      setRequestCount(count);
      message.error(`Error: GetDIDArticleInfo is fail: ${err}`)
      return;
    }
    setArticleInfo(ret)
  }

  const loadLog = async (address:string) => {
    Async(async () => {
      const request = new GetDIDArticleLogRequest();
      request.address = address;
      const [ret, err] = await GetDIDArticleLog(request)
      if (err) {
        const count = requestCount + 1;
        setRequestCount(count);
        message.error(`Error: GetDIDArticleInfo is fail: ${err}`)
        return;
      }
      // TODO APP端logs展示排查了version=1的数据，浏览器端需要查看Salt、initCode等字段，需要跟产品确认
      setLogs(ret.items)
    })

  }

  const buildContent = (content:string|number|string[], config: {color: string, copy: boolean, fn?: Function } = {color: '#000000', copy: false}):JSX.Element => {
    if (Array.isArray(content)) {
      return (
        <>
          {
            content.map((item, index) => (
              <div style={{color: config.color}} key={index}>
                {item}
              </div>
            ))
          }
        </>
      );

    }
    return <span style={{color: config.color}} onClick={() => (config.fn ? config.fn() : () => {})}>
      { formatNum(content) }
      { config.copy && <img className={styles.copyImg} src={copyImg} alt="" onClick={(e: any) => copyFun(e, content)}/>}
    </span>
  }

  const buildButton = (name: string, direction: 'up' | 'down'): JSX.Element => {
      const click = () => {
          setDirect(direction)
      }

      return (
        <div className={styles.wrapBtn}>
          <div onClick={click} className={styles.button}>
            <span>{name}</span>
            <img src={direction === 'up' ? down : up} alt=""/>
          </div>
        </div>
      );
  }

  const buildTitle = (item:GetDIDArticleLogItem) => {
    const typeMap = new Map([
      [UpdateType.UpdateTypeContent, formatMessage({id: 'update_content'})],
      [UpdateType.UpdateTypeAbstract, formatMessage({id: 'update_settings'})],
      [UpdateType.UpdateTypeTokenAddress, formatMessage({id: 'update_settings'})]
    ])
    return `V${item.version} ${typeMap.get(item.updateType)}`;
  }

  const updateArticle = async (version:number) => {
    await loadArticleInfo(address, version)
    const dom = document.getElementById('containerGroup')
    dom && dom.scrollIntoView({ behavior: "smooth", block: "start" })
  }

  const clickCreator = (str:string) => {
    navigate(`/user/${str}`)
  }

  const updateInfo = (address:string) => {
    Async(async () => {
      load(address)
      const dom = document.getElementById('containerGroup')
      dom && dom.scrollIntoView({ behavior: "smooth", block: "start" })
    })
  }
  const Logs = () => {
        return <>{ logs.length > 0 && <div className={classNames([styles.overviewWrap, window.innerWidth > 1330 && styles.logsOverviewWrap])}>
            {
                logs.map(item => {
                    return (
                        <div className={styles.logItem} key={item.verHash}>
                            <div className={styles.line}>
                                <div className={styles.quan}></div>
                                <div className={styles.upJianTou}>
                                    <img src={upJianTou} alt=""/>
                                </div>
                            </div>
                            <div className={styles.logItemTitle}>{ buildTitle(item) }</div>
                            <div className={styles.logItemTime}>{ formatTime(item.updateTime) }</div>
                            <div className={styles.logItemVerHash}>verHash: <span className={styles.logItemVerHashText} onClick={ () => updateArticle(item.version) }>{  item.verHash }</span></div>
                        </div>
                    );
                })
            }

        </div>
        }</>
  }

    if(requestCount === 1) {
        return <LoadFail />
    }
    return (
        <div className={styles.container} id={'containerGroup'}>
            <Header />
            <div className={styles.titleWrap}>
                <div className={styles.title}><FormattedMessage id="content_address" /></div>
                <div className={styles.content}>
                    <span>{ articleInfo.address }</span>
                    <img className={styles.copyImg} src={copyImg} alt="" onClick={(e: any) => { copyFun(e, articleInfo.address)}}/>
                </div>
            </div>
            {
                window.innerWidth > 1330 ? <div className={styles.logPanelWrap}>
                    <Panel dataUpdateTime={dataUpdateTime} statisticsData={statisticsData} />
                    <Logs />
                </div> : <Panel dataUpdateTime={dataUpdateTime} statisticsData={statisticsData} />
            }
            <div className={styles.overviewWrap}>
                <div className={styles.title}><FormattedMessage id="overview" /></div>
                <div className={styles.infoWrap}>
                    <Cell title={`${formatMessage({id: 'creator'})}：`} blue={true} content={buildContent(articleInfo.creatorAddress, {color: '#3470E9', copy: true, fn: () => clickCreator(articleInfo.creatorAddress)})}></Cell>
                    <Cell title={`${formatMessage({id: 'dcirclescan_content_title'})}：`} content={buildContent(articleInfo.title)}></Cell>
                    <Cell title={`${formatMessage({id: 'summary'})}：`} content={buildContent(articleInfo.abstract)}></Cell>
                    <Cell title={`${formatMessage({id: 'message_token'})}：`} content={buildContent(articleInfo.token)}></Cell>
                    <Cell title={`${formatMessage({id: 'version_number'})}：`} content={buildContent( articleInfo.version )}></Cell>
                    {direct ===  'down' && buildButton(formatMessage({id: 'more'}), 'up')}
                  {
                    direct ===  'up' && (
                      <div className={styles.wrap_cell}>
                        <div className={styles.line}></div>
                        <Cell title={`${formatMessage({id: 'update_time'})}：`} content={buildContent(formatTime(articleInfo.updateTime))}></Cell>
                        <Cell title={`${formatMessage({id: 'creative_time'})}：`} content={buildContent(formatTime(articleInfo.createTime))}></Cell>
                        <Cell title="init_code：" content={buildContent(articleInfo.initCode)}></Cell>
                        <Cell title="Salt：" content={buildContent(articleInfo.salt)}></Cell>
                        <Cell title={`${formatMessage({id: 'eigenvalues'})}：`} content={buildContent(articleInfo.featureVale)}></Cell>
                        {buildButton(formatMessage({id: 'put_away'}), 'down')}
                      </div>
                    )
                  }

                </div>
            </div>
            {window.innerWidth < 1330 && <Logs />}
          <ContentList address={address} />
        </div>
    )
}
