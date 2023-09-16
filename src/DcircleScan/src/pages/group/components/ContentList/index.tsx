import styles from "./index.module.scss";
import {message, Popover, Table} from "antd";
import React, { useEffect, useRef, useState} from "react";
import Spin from '../../../user/component/Spin';
import {useNavigate} from "react-router-dom";
import {
    GetUserStatByDIDRole,
    GetUserStatByDIDRoleRequest,
    GetUserStatByDIDRoleResponse
} from "../../../../api/GetUserStatByDIDRole";
import {
    GetUserTxListByDIDRole,
    GetUserTxListByDIDRoleRequest,
    GetUserTxListByDIDRoleResponseItem1
} from "../../../../api/GetUserTxListByDIDRole";
import {DIDRole} from "../../../../api/DIDRole";
import den from "../../../den.png";
import formatTime from "../../../../helper/formatTime";
import formatNum from "../../../../helper/formatNum";
import {FormattedMessage} from "react-intl";
import {useIntl} from "react-intl";
import {getUs} from "../../../../DIDBrowser";
import {DBDIDArticleRole} from "../../../../db/DBDIDArticleRole";
import {Async} from "../../../../Async";
import {GetDScan} from "../../../../db/db";
import buildId = DBDIDArticleRole.buildId;

export default function Create({address}: {address:string}) {
    const navigate = useNavigate();
    const [statList, setStatList] = useState<GetUserStatByDIDRoleResponse>(new GetUserStatByDIDRoleResponse())
    const [txList, setTxList] = useState<GetUserTxListByDIDRoleResponseItem1[]>([])
    const loadMoreRef = useRef<boolean>(true);
    const [loadMore, setLoadMore] = useState(true);
    const [noMore, setNoMore] = useState(false)
    const { formatMessage } = useIntl();

    const columnsStat = [
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumption_times" defaultMessage="交易次数" /></span>,
            dataIndex: 'cTimes',
            key: 'cTimes',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.cTimes)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="message_consumers" defaultMessage="消费人数" /></span>,
            dataIndex: 'cNums',
            key: 'cNums',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.cNums)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="total_content" defaultMessage="交易篇数"  /></span>,
            dataIndex: 'nNums',
            key: 'nNums',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.nNums)}</span>
            },
        },
        {
            title: <span className={styles.titleS200}><FormattedMessage id="statistics_Time" /></span>,
            dataIndex: 'dataUpdateTime',
            key: 'dataUpdateTime',
            render: (_:any, record:any) => {
                return <span className={styles.cell200} >{formatTime(record.dataUpdateTime)} PM +UTC</span>
            },
        }
    ];

    const formatMsg = (address: string) => {
        if (!address || address === '-') return '-';
        let result = address.substring(0, 8); // 获取前四个字符
        result += "..."; // 添加省略号
        result += address.substring(address.length - 8); // 获取后三个字符
        return result;
    }
    const columnsTx =  [
        {
            title: <span className={styles.titleS}>Txn Hash</span>,
            dataIndex: 'txHash',
            key: 'txHash',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.txHash || record.txHash === '-') return
                    navigate(`/transaction/${record.id}`)
                }
                return <span onClick={onclick} className={styles.defineCell} >{record.txHash}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumer" /></span>,
            dataIndex: 'buyUserId',
            key: 'buyUserId',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.buyUserId || record.buyUserId === '-') return
                    navigate(`/user/${record.buyUserId}`)
                }
                return <div className={styles.wrapCell}>
                    { record.buyUserNotInChat && <Popover placement="bottom" content={formatMessage({id: 'person_no_in_group'})} trigger="click">
                        <img src={den} className={styles.tipsIcon} alt=""/>
                      </Popover>
                    }
                    <span onClick={onclick} className={styles.defineCell} >{formatMsg(record.buyUserId)}</span>
                </div>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="content" /></span>,
            dataIndex: 'didAddress',
            key: 'didAddress',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.didAddress || record.didAddress === '-') return
                    navigate(`/article/${record.didAddress}`)
                }
                return <div className={styles.wrapCell}>
                    { record.didNotInChat &&  <Popover placement="bottom" content={formatMessage({id: 'content_removed_group'})} trigger="click">
                        <img src={den} className={styles.tipsIcon} alt=""/>
                      </Popover>
                    }
                    <div onClick={onclick} className={styles.defineCell} >
                        <span>{formatTextMessage(record.didAddress)}</span>
                        <div className={styles.showText}>{record.didContent}</div>
                    </div>
                </div>
            },
        },
        {
            title: <span className={styles.titleS200}><FormattedMessage id="transaction_time" /></span>,
            dataIndex: 'buyTime',
            key: 'buyTime',
            render: (_:any, record:any) => {
                return <span className={styles.cell200}>{formatTime(record.buyTime)} PM +UTC</span>
            },
        }
    ];
    const formatTextMessage = (address: string) => {
        if (!address || address === '-') return '-';
        let result = address.substring(0, 8); // 获取前四个字符
        result += "..."; // 添加省略号
        result += address.substring(address.length - 8); // 获取后三个字符
        return result;
    }

    useEffect(() => {
        const handleScroll = () => {
            const scrollHeight = document.documentElement.scrollHeight - 200;
            const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
            const clientHeight = document.documentElement.clientHeight;
            if (noMore) return;
            if (scrollTop + clientHeight >= scrollHeight) {
                if (loadMoreRef.current) {
                    loadMoreRef.current = false;
                    setLoadMore(false)
                    loadTx().then()
                }
            }
        }
        window.addEventListener('scroll', handleScroll);
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, [txList.length, noMore])

    useEffect(() => {
        Async(async () => {
            getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
                if (!event.ids.includes(address)) return
                if (event.source != "group_content_list") return;
                await loadStatFromDB();
            })
            loadTx().then()
            await loadStatFromDB();
            await loadStatFromServer();
        })

    }, [])



    const loadTx = async () => {
        const request = new GetUserTxListByDIDRoleRequest();
        request.address = address;
        request.size = 10;
        request.startTxTime = new Date().getTime();
        request.role = DIDRole.SingleGroupStat;
        const [ret, err] = await GetUserTxListByDIDRole(request);
        loadMoreRef.current = ret.length < 10;
        setLoadMore(true)
        if (err) {
            message.error(`Error: GetUserTxListByDIDRoleCreator is fail: ${err}`)
            return;
        }
        setNoMore(ret.length < 10)

        if (!txList.length) {
            setTxList(ret)
            return
        }
        setTxList(prev => [...prev, ...ret]);
    }

    const loadStatFromServer = async () => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address = address;
        request.role = DIDRole.SingleGroupStat;
        await GetUserStatByDIDRole(request, 'group_content_list');
    }

    const loadStatFromDB = async () => {

        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.Group));
        if (err) {
            return;
        }
        setStatList(ret)
    }
    return (
      <>
          <div className={styles.tableContainer}>
              <div className={styles.tableWrap}>
                  <Table rowKey="dataUpdateTime" scroll={{ x: true }} columns={columnsStat} dataSource={[statList]} pagination={false} />
              </div>
              <div className={styles.tableWrap}>
                  <Table rowKey="buyTime" scroll={{ x: true }} columns={columnsTx} dataSource={txList} pagination={false} />
              </div>
          </div>
          { !loadMoreRef.current && <Spin/> }
      </>
    )
}
