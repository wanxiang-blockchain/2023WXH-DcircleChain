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
import formatTime from "../../../../helper/formatTime";
import formatNum from "../../../../helper/formatNum";
import ques from "../../../question.png";
import classNames from "classnames";
import {FormattedMessage} from "react-intl";
import {Async} from "../../../../Async";
import {DBDIDArticleRole} from "../../../../db/DBDIDArticleRole";
import {GetDScan} from "../../../../db/db";
import {getUs} from "../../../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;


export default function Create({address}: {address:string}) {
    const navigate = useNavigate();
    const [statList, setStatList] = useState<GetUserStatByDIDRoleResponse>(new GetUserStatByDIDRoleResponse())
    const [txList, setTxList] = useState<GetUserTxListByDIDRoleResponseItem1[]>([])
    const loadMoreRef = useRef<boolean>(true);
    const [loadMore, setLoadMore] = useState(true);
    const [noMore, setNoMore] = useState(false)

    const columnsStat = [
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumption_times" /></span>,
            dataIndex: 'cTimes',
            key: 'cTimes',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.cTimes)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="message_consumers" /></span>,
            dataIndex: 'cNums',
            key: 'cNums',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.cNums)}</span>
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
            title: <span className={styles.titleS}><FormattedMessage id="disseminator" defaultMessage="传播人数" /></span>,
            dataIndex: 'tNums',
            key: 'tNums',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.tNums)}</span>
            },
        },
        {
            title: '',
            dataIndex: '',
            key: '',
            render: (_:any, record:any) => {
                return <span className={styles.cell}></span>
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
            title: <span className={styles.titleS}><FormattedMessage id="txn_hash" defaultMessage="交易哈希" /></span>,
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
                return <span onClick={onclick} className={styles.defineCell} >{formatMsg(record.buyUserId)}</span>
            },
        },
        {
            title: <div className={classNames([styles.wrapCell, styles.WithIconCell])}>
                <Popover placement="bottom" content={<span><FormattedMessage id="communication_group_records_logo" /></span>} trigger="click">
                    <img src={ques} className={styles.tipsIcon} alt=""/>
                </Popover>
                <span className={styles.titleSI}><FormattedMessage id="communication_group_address" defaultMessage="交易群" /></span>
            </div>,
            dataIndex: 'transferChat',
            key: 'transferChat',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.transferChat || record.transferChat === '-') return
                    navigate(`/group/${record.transferChat}`)
                }
                return <div onClick={onclick} className={styles.defineCell} >
                    <span>{formatMsg(record.transferChat)}</span>
                    <div className={styles.showText}>{record.groupContent}</div>
                </div>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="disseminator" /></span>,
            dataIndex: 'transferUserId',
            key: 'transferUserId',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.transferUserId || record.transferUserId === '-') return
                    navigate(`/user/${record.transferUserId}`)
                }
                return <span onClick={onclick} className={styles.defineCell} >{formatMsg(record.transferUserId)}</span>
            },
        },
        {
            title: <span className={styles.titleS}>verHash</span>,
            dataIndex: 'buyDidBlockRootHash',
            key: 'buyDidBlockRootHash',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatMsg(record.buyDidBlockRootHash)}</span>
            },
        },
        {
            title: <span className={styles.titleS200}><FormattedMessage id="statistics_Time" /></span>,
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
                // await loadStatFromDB();
            })
            loadTx().then()
            await loadStatFromDB()
            // await loadStatFromServer();
        })
    }, [])

    const loadTx = async () => {
        const request = new GetUserTxListByDIDRoleRequest();
        request.address = address;
        request.size = 10;
        request.startTxTime = (txList.length > 0 && txList[txList.length - 1].transferTime) || new Date().getTime();
        request.role = DIDRole.ArticleStat;
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

    const loadStatFromServer = async () => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address = address;
        request.role = DIDRole.SingleGroupStat;
        await GetUserStatByDIDRole(request);
    }
    const loadStatFromDB = async () => {
        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.ArticleStat));
        if (err) {
            return;
        }
        if (err) {
            message.error(`Error: GetUserStatByDIDRole is fail: ${err}`)
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
          { !noMore && <Spin/> }
      </>
    )
}
