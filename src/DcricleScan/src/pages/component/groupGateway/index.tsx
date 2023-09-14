import styles from "./index.module.scss";
import React, {useEffect, useRef, useState} from "react";
import message from "../../article/image/message.png";
import classNames from "classnames";
import upJianTou from '../../article/image/upJianTou.png'
import more from '../../article/image/more.png'
import noData from '../../article/image/noData.png'
import {getHttp, getNavigate} from "../../../helper/handleUrl";
import copy from "copy-to-clipboard";
import down from "../../article/image/down.png";
import up from "../../article/image/up.png";
import {FormattedMessage, useIntl} from "react-intl";
import {getDisplayMessage, MsgContent} from "../../../helper/Message";
import {PullEssenceMessage, PullEssenceMessageRequest} from "../../../api/PullEssenceMessage";
import {DidTag, GetChatDIDArticleTag} from "../../../api/GetChatDIDArticleTag";
import formatTime from "../../../helper/formatTime";
import {GetChatDIDArticleByTag, GetChatDIDArticleByTagRequest, ChatDIDArticleItem} from "../../../api/GetChatDIDArticleByTag";
import { MsgCellBuilderCreator } from '../../cell/MsgCellFactory';
import {Async} from "../../../Async";
import {OssImage} from "../../../oss/OssImage";
import NoImage from '../NoImage';
import {getIsDcircleEnv, getIsWxEnv} from "../../../helper/getRunningEnv";
import {getUs} from "../../../DIDBrowser";
import {DBGroup} from "../../../db/DBGroup";
import {AppScheme} from "../../../config";
import {BusHelper} from "../../../helper/BusHelper";
interface Props {
    address:string
    essenceKey:string
}
export default function GroupGateway(props: Props) {
    const [tabIndex, setTabIndex] = useState<number>(0);
    const page = useRef(1); // 页码
    const [direct, setDirect] =  useState<'up'|'down'>('down');
    const { formatMessage } = useIntl();
    const [isJH, setIsJH] = useState<boolean>(false)
    const [JHList, setJHList] = useState<MsgContent[]>([]);
    const [messageHeight, setMessageHeight] = useState<number>(0);
    const [tagList, setTagList] = useState<DidTag[]>([]);
    const [didArticleList, setDidArticleList] = useState<ChatDIDArticleItem[]>([]);
    const [isDidArticle, setIsDidArticle] = useState<boolean>(false);
    const [isDcircle, setIsDcircle] = useState<boolean>(false);
    const JHSearchList = useRef<MsgContent[]>([]);
    const hasMore = useRef<boolean>(true);
    const prevTargetBottom = useRef<number>(0);
    const tabActive = useRef<number>(0);
    const currentPage = useRef<number>(0);
    const loading = useRef<boolean>(false);
    const startGet = useRef<boolean>(false);
    const timers = useRef<NodeJS.Timer>()

    useEffect(() => {
        setIsDcircle(getIsDcircleEnv())
    }, [])
    useEffect(() => {
        if(props.address.length <= 0) return
        getDidTag(props.address).then();
        if(props.essenceKey.length <= 0) {
            setIsJH(true);
            return;
        }
        handlePullEssenceMessage(props.address, props.essenceKey).then();
    }, [props.address, props.essenceKey])
    const pullEssenceMessageFromServer = async (address:string) => {
        if (startGet.current) return;
        startGet.current = true;
        const request = new PullEssenceMessageRequest();
        request.chatId = address;
        if (JHList.length > 0) {
            request.end = JHSearchList.current.sort((a, b) => a.seq - b.seq)[0].seq
        }
        await PullEssenceMessage(request);
        startGet.current = false;
    }
    // 获取精华数据
    const handlePullEssenceMessage = async (address:string, essenceKey:string) => {
        if (startGet.current) return;
        startGet.current = true;
        const request = new PullEssenceMessageRequest();
        request.chatId = address;
        if (JHList.length > 0) {
            request.end = JHSearchList.current.sort((a, b) => a.seq - b.seq)[0].seq
        }
        const [ret, err] = await PullEssenceMessage(request);
        startGet.current = false;
        if(err) {
            return;
        }
        const res = await getDisplayMessage(ret.items, essenceKey);
        const list:MsgContent[] = [];
        for(let i = 0; i < res.length; i++) {
            const it = res[i]
            if (it[1] != null) continue
            list.push(it[0] as MsgContent)
        }
        setIsJH(list.length === 0 && request.end === -1);
        if (list.length <= 0) {
            hasMore.current = false
            return;
        }

        setJHList(prevList => [...prevList, ...list])
        JHSearchList.current = JHSearchList.current.concat(list);
        hasMore.current = true
        setTimeout(() => {
            const messageHeight = document.getElementById('messageList')!.getBoundingClientRect()!.height;
            setMessageHeight(messageHeight);
        }, 200)
    }

    // 获取汇编tag
    const getDidTag = async (address:string) => {
        const [ret, err] = await GetChatDIDArticleTag(address);
        if (err) return;
        setTagList(ret.items)
    }
    // 根据tag获取did文章
    const getDidArticle = async (address:string, id: string) => {
        const request = new GetChatDIDArticleByTagRequest();
        request.chatId = address;
        request.tagId = id;
        request.pageIndex = currentPage.current;
        request.pageSize = 10;
        const [ret, err] = await GetChatDIDArticleByTag(request);
        if (err) {
            return;
        };
        if (ret.items.length <= 0) {
            if(currentPage.current === 0) {
                setDidArticleList([]);
                setIsDidArticle(true);
            }
            hasMore.current = false
            return
        };
        hasMore.current = true
        currentPage.current = currentPage.current + 1;
        if (currentPage.current === 1) {
            setDidArticleList(ret.items)
            return;
        }
        setDidArticleList(prevList => [...prevList, ...ret.items])
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

    useEffect(() => {
        const targetNode = document.getElementById('GATEWAY')
        function handleScroll() {
            if (!targetNode) return
            const targetBottom = targetNode.getBoundingClientRect().bottom;
            let scrollDir = targetBottom > prevTargetBottom.current ? 'up' : 'down'
            prevTargetBottom.current = targetBottom;
            if (scrollDir === 'up') { // 向上滚动不允许加载分页数据
                return;
            }
            if (targetBottom < 1000 && direct ===  'up' && hasMore.current) {
                Async( async () => {
                    if (loading.current) return;
                    loading.current = true

                    if (tabActive.current === 0) {
                        // 精华消息的加载更多
                        await handlePullEssenceMessage(props.address, props.essenceKey);
                        // await pullEssenceMessageFromServer(props.address);
                        loading.current = false
                        return;
                    }
                    // TODO 调用接口分页
                    await getDidArticle(props.address, tagList[tabActive.current-1].id);
                    loading.current = false
                })
            }
        }
        window.addEventListener('scroll', handleScroll);
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, [direct])

    const openDcircle = async (item:MsgContent) => {
        // 微信中打开，直接弹窗引导在浏览器中打开
        if (getIsWxEnv()) {
            Async(async () => {
                await getUs().nc.post(new DBGroup.ModalChangedEvent(['true']))
            })
            return;
        }

        if (getIsDcircleEnv()) {
            await BusHelper.HandleToChatMessage(props.address, item.msgId)
            return;
        }
        openApp()
    }
    const openApp = () => {
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
    const buildTitle = (item:DidTag) => {
        let name = item.name;
        // if (item.count <= 0) return name
        name += ` (${item.count})`
        return name;
    }

    const tabHandle = (index: number, id: string) => {
        setTabIndex(index + 1)
        tabActive.current = index + 1;
        loading.current = false;
        hasMore.current = true;
        currentPage.current = 0;
        setIsDidArticle(false);
        getDidArticle(props.address, id).then()
    }

    const getMessage = (item: MsgContent) => {
        return MsgCellBuilderCreator().get(item, props.address)
    }
    const handleTouchStart = (e: React.TouchEvent<HTMLDivElement>):void => {
        e.preventDefault();
    }

    const openDcirlce = async (item: ChatDIDArticleItem) => {
        if (getIsDcircleEnv()) {
            await BusHelper.HandleDidNcApp(props.address, item.didAddress, item.msgId, item.nodupId);
            return
        }
        toDownload();
    }

    const toDownload = () => {
        // 微信中打开，直接弹窗引导在浏览器中打开
        if (getIsWxEnv()) {
            Async(async () => {
                await getUs().nc.post(new DBGroup.ModalChangedEvent(['true']))
            })
            return;
        }

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

    return <div className={styles.groupContainer} id="GATEWAY">
        <div className={styles.tabList}>
            <div onTouchStart={handleTouchStart} className={classNames([styles.tabItem, tabIndex === 0 && styles.tabItemSelect])} onClick={() => {
                tabActive.current = 0;
                setTabIndex(0)
            }}><FormattedMessage id="group_setting_essential_news" defaultMessage="Essential" /></div>
            <div className={classNames([tabIndex === 0 && styles.oneLine])}></div>
            <div className={styles.line}></div>
            <div className={styles.resourceTabWrap}>
                {
                    tagList.map((item, index) => {
                        return (<div
                            key={index}
                            className={classNames([styles.tabItem, tabIndex === index + 1 && styles.tabItemSelect])}
                            onClick={() => tabHandle(index, item.id)}
                        >
                            <div className={styles.tabText}>{ buildTitle(item) }</div>
                            <div className={classNames([tabIndex === index + 1 && styles.activeLine])}></div>
                        </div>)
                    })
                }
            </div>
        </div>
        <div className={styles.messageList} style={{display:  tabIndex === 0 ? 'block' : 'none', maxHeight: direct ===  'down' ? '440px' : ''}}>
            <div id="messageList">
                {
                    JHList.map((item, index) => {
                        return (
                            <div key={index} className={styles.messageItem}>
                                <div className={styles.infoWrap}>
                                    <div className={styles.time}>{formatTime(item.createTime)}</div>
                                    <img className={styles.infoImg} src={message} alt="" onClick={() => openDcircle(item)}/>
                                </div>
                                {getMessage(item)}
                            </div>)
                    })
                }
            </div>
            {(direct ===  'down' && messageHeight > 440) && buildButton(formatMessage({id: 'more'}), 'up')}
            {
                isJH && <div className={styles.noDataWrap}>
                    <img className={styles.noDataImg} src={noData} alt=""/>
                    <div className={styles.noDataText}><FormattedMessage id="group_setting_essence_message_empty_data" defaultMessage="No news yet~" /></div>
                </div>
            }
        </div>
        <div className={styles.resourceFileWrap} style={{display:  tabIndex !== 0 ? 'block' : 'none', maxHeight: direct ===  'down' ? '440px' : ''}}>
            {
                didArticleList.map((item, index) => {
                    return (
                        <div className={styles.logItem} key={index}>
                            <div className={styles.line}>
                                <div className={styles.quan}></div>
                                <div className={styles.upJianTou}>
                                    <img src={upJianTou} alt=""/>
                                </div>
                            </div>
                            <div className={styles.fileItem} onClick={() => openDcirlce(item)}>
                                {/*<img className={styles.fileImg} src={test} alt=""/>*/}
                                <OssImage
                                    objectId={item.abstractImage.objectId}
                                    objectKey={item.abstractImage.key}
                                    style={{height: '64px', borderRadius: '8px', width: '64px', objectFit: 'cover'}}
                                    placeholder={<NoImage style={{width: '64px', height: '64px'}}/>}
                                />
                                {/*<img className={styles.fileMore} src={more} alt=""/>*/}
                                <div className={styles.fileInfo}>
                                    <div className={styles.fileTitle}>{item.title}</div>
                                    <div className={styles.fileContent}>{item.abstract}</div>
                                    <div className={styles.fileAuthorInfo}>
                                        <div className={styles.fileTime}>{item.creatorName}  |  {formatTime(item.createTime)}</div>
                                        {/*<div className={styles.fileFlag}>我创作的</div>*/}
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                })
            }
            {(direct ===  'down' && didArticleList.length > 3) && buildButton(formatMessage({id: 'more'}), 'up')}
            {
                isDidArticle && <div className={styles.noDataWrap}>
                    <img className={styles.noDataImg} src={noData} alt=""/>
                    <div className={styles.noDataText}><FormattedMessage id="default_map_group_did_no_content" defaultMessage="No data yet~" /></div>
                </div>
            }
        </div>
    </div>
}
