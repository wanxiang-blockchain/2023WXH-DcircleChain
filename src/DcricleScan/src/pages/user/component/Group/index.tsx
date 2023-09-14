import styles from "../styles/index.module.scss";
import classNames from "classnames";
import {message, Table} from "antd";
import React, {useEffect, useRef, useState} from "react";
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
import Spin from '../Spin'
import formatTime from "../../../../helper/formatTime";
import formatNum from "../../../../helper/formatNum";
import {FormattedMessage} from "react-intl";
import {DBDIDArticleRole} from "../../../../db/DBDIDArticleRole";
import {GetDScan} from "../../../../db/db";
import {getUs} from "../../../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;


export default function Group({address, upddateInfo}: {address:string, upddateInfo: (address:string) => void}) {
    const navigate = useNavigate();
    const [statList, setStatList] = useState<GetUserStatByDIDRoleResponse>(new GetUserStatByDIDRoleResponse())
    const [txList, setTxList] = useState<GetUserTxListByDIDRoleResponseItem1[]>([])
    const loadMoreRef = useRef<boolean>(true);
    const [loadMore, setLoadMore] = useState(true);
    const [noMore, setNoMore] = useState(false)
    const [inAddress, setInAddress] = useState(address);
    const currentAddress = useRef<string>(address)
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
            title: <span className={styles.titleS}><FormattedMessage id="communication_group" defaultMessage="交易群数" /></span>,
            dataIndex: 'gNums',
            key: 'gNums',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.gNums)}</span>
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
            title: <span className={styles.titleS}><FormattedMessage id="number_dissemination_articles" defaultMessage="传播篇数" /></span>,
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
                return <span className={styles.cell200}>{formatTime(record.dataUpdateTime)} PM +UTC</span>
            }
        }
    ];

    const formatMessage = (address: string) => {
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
            title: <span className={styles.titleS}><FormattedMessage id="communication_group_address" defaultMessage="交易群" /></span>,
            dataIndex: 'transferChat',
            key: 'transferChat',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.transferChat || record.transferChat === '-') return;
                    navigate(`/group/${record.transferChat}`)
                }
                return <div onClick={onclick} className={styles.defineCell} >
                    <span>{formatMessage(record.transferChat)}</span>
                    <div className={styles.showText}>{record.groupContent}</div>
                </div>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumer" /></span>,
            dataIndex: 'buyUserId',
            key: 'buyUserId',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.buyUserId || record.buyUserId === '-') return
                    upddateInfo(record.buyUserId);
                    setStatList(new GetUserStatByDIDRoleResponse())
                    setTxList([])
                    setInAddress(record.buyUserId)
                    currentAddress.current = record.buyUserId
                    load(record.buyUserId).then();
                }
                return <span onClick={onclick} className={styles.defineCell} >{formatMessage(record.buyUserId)}</span>
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
                return <div onClick={onclick} className={styles.defineCell} >
                    <span>{formatMessage(record.didAddress)}</span>
                    <div className={styles.showText}>{record.didContent}</div>
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
                    loadTx(inAddress).then()
                }
            }
        }
        window.addEventListener('scroll', handleScroll);
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, [txList.length, noMore])

    useEffect(() => {
        getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
            if (!event.ids.includes(currentAddress.current)) return;
            if (event.source != 'user_group') return;
            await loadStatFromDB(currentAddress.current);
        })
        load(inAddress).then()
    }, [])

    const load = async (address:string) => {
        loadTx(address).then()
        await loadStatFromDB(address)
        await loadStatFromServer(address)
    }


    const loadTx = async (address:string) => {
        const request = new GetUserTxListByDIDRoleRequest();
        request.address = address;
        request.size = 10;
        request.startTxTime = (txList.length > 0 && txList[txList.length - 1].transferTime) || new Date().getTime();
        request.role = DIDRole.Group;
        const [ret, err] = await GetUserTxListByDIDRole(request);
        loadMoreRef.current = ret.length >= 10;
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

    const loadStatFromDB = async (address:string) => {
        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.Group));
        if (err) {
            return;
        }
        setStatList(ret)
    }

    const loadStatFromServer = async (address:string) => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address = address;
        request.role = DIDRole.Group;
        await GetUserStatByDIDRole(request, 'user_group');
    }

    return (
      <>
          <div className={styles.tableContainer}>
              <div className={classNames([styles.tableWrap, styles.fiveTableWrap])}>
                  <Table rowKey="dataUpdateTime" scroll={{ x: true }} columns={columnsStat} dataSource={[statList]} pagination={false} />
              </div>
              <div className={classNames([styles.tableWrap, styles.fiveTableWrap])}>
                  <Table rowKey="buyTime" scroll={{ x: true }} columns={columnsTx} dataSource={txList} pagination={false} />
              </div>
          </div>
          { !noMore && <Spin/> }
      </>
    )
}
