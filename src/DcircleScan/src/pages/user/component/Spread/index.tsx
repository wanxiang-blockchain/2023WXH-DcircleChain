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
import {FormattedMessage, useIntl} from "react-intl";
import {DBDIDArticleRole} from "../../../../db/DBDIDArticleRole";
import {GetDScan} from "../../../../db/db";
import {getUs} from "../../../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;
import tabIcon1 from "../../image/tabIcon1.png";
import tabIcon2 from "../../image/tabIcon2.png";
import tabDown from "../../image/tabDown.png";
import {
    GetUserInviteTxList,
    GetUserInviteTxListRequest,
    GetUserInviteTxListResponseItem
} from "../../../../api/GetUserInviteTxList";
import {GetDIDAllRoleStat, GetDIDAllRoleStatRequestResponse} from "../../../../api/GetDIDAllRoleStat";
interface Prop {
    address:string
    updateInfo: (address:string) => void
}
/*
* 传播
* **/
export default function Spread({address, updateInfo}: Prop) {
    const navigate = useNavigate();
    const [statList, setStatList] = useState<GetUserStatByDIDRoleResponse>(new GetUserStatByDIDRoleResponse())
    const [inviteStatList, setInviteStatList] = useState<GetDIDAllRoleStatRequestResponse>(new GetDIDAllRoleStatRequestResponse())
    const [txList, setTxList] = useState<GetUserTxListByDIDRoleResponseItem1[]>([])
    const [txInviteList, setTxInviteList] = useState<GetUserInviteTxListResponseItem[]>([])
    const loadMoreRef = useRef<boolean>(true);
    const [loadMore, setLoadMore] = useState(true);
    const [noMore, setNoMore] = useState(false);
    const [isShowList, setIsShowList] = useState(false);
    const [tabType, setTabType] = useState<'content' | 'group'>('content');
    const [inAddress, setInAddress] = useState(address);
    const currentAddress = useRef<string>(address);
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
            title: <span className={styles.titleS}><FormattedMessage id="total_content" defaultMessage="交易篇数" /></span>,
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
    const columnsStatInvite = [
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumption_times" defaultMessage="交易次数" /></span>,
            dataIndex: 'cTimes',
            key: 'cTimes',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.cTimes)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="personal_did_proof_broadcasting_power_enter_people" defaultMessage="进群人数" /></span>,
            dataIndex: 'joinUserCount',
            key: 'joinUserCount',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.joinUserCount)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="personal_did_proof_broadcasting_power_share_group" defaultMessage="分享群数" /></span>,
            dataIndex: 'shareGroupCount',
            key: 'shareGroupCount',
            render: (_:any, record:any) => {
                return <span className={styles.cell}>{formatNum(record.shareGroupCount)}</span>
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
            title: <span className={styles.titleSI}><FormattedMessage id="communication_group_address" defaultMessage="交易群" /></span>,
            dataIndex: 'transferChat',
            key: 'transferChat',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.transferChat || record.transferChat === '-') return;
                    navigate(`/group/${record.transferChat}`)
                }
                return <div onClick={onclick} className={styles.defineCell} >
                    <span>{formatMsg(record.transferChat)}</span>
                    <div className={styles.showText}>{record.groupContent}</div>
                </div>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumer" defaultMessage="消费者" /></span>,
            dataIndex: 'buyUserId',
            key: 'buyUserId',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.buyUserId || record.buyUserId === '-') return
                    updateInfo(record.buyUserId);
                    setStatList(new GetUserStatByDIDRoleResponse())
                    setTxList([])
                    setInAddress(record.buyUserId)
                    currentAddress.current = record.buyUserId;
                    load(record.buyUserId).then();
                }
                return <span onClick={onclick} className={styles.defineCell} >{formatMsg(record.buyUserId)}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="content" defaultMessage="内容" /></span>,
            dataIndex: 'didAddress',
            key: 'didAddress',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.didAddress || record.didAddress === '-') return
                    navigate(`/article/${record.didAddress}`)
                }
                return <span onClick={onclick} className={styles.defineCell} >
                    <span>{formatMsg(record.didAddress)}</span>
                    <div className={styles.showText}>{record.didContent}</div>
                </span>
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
    const columnsInviteTx = [
        {
            title: <span className={styles.titleS}>Txn Hash</span>,
            dataIndex: 'txHash',
            key: 'txHash',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.txHash || record.txHash === '-') return
                    navigate(`/transactionGroup/${record.id}`)
                }
                return <span onClick={onclick} className={styles.defineCell} >{record.txHash}</span>
            },
        },
        {
            title: <span className={styles.titleS}><FormattedMessage id="consumer" defaultMessage="消费者" /></span>,
            dataIndex: 'inviteeUid',
            key: 'inviteeUid',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.inviteeUid || record.inviteeUid === '-') return
                    updateInfo(record.inviteeUid);
                    setStatList(new GetUserStatByDIDRoleResponse())
                    setTxList([])
                    setInAddress(record.inviteeUid)
                    currentAddress.current = record.inviteeUid;
                    load(record.inviteeUid).then();
                }
                return <span onClick={onclick} className={styles.defineCell} >{formatMsg(record.inviteeUid)}</span>
            },
        },
        {
            title: <span className={styles.titleSI}><FormattedMessage id="communication_group_address" defaultMessage="交易群" /></span>,
            dataIndex: 'transferChat',
            key: 'transferChat',
            render: (_:any, record:any) => {
                const onclick = () => {
                    if (!record.transferChat || record.transferChat === '-') return;
                    navigate(`/group/${record.transferChat}`)
                }
                return <div onClick={onclick} className={styles.defineCell} >
                    <span>{formatMsg(record.transferChat)}</span>
                    <div className={styles.showText}>{record.joinChatId}</div>
                </div>
            },
        },
        {
            title: <span className={styles.titleS200}><FormattedMessage id="transaction_time" /></span>,
            dataIndex: 'joinTime',
            key: 'joinTime',
            render: (_:any, record:any) => {
                return <span className={styles.cell200}>{formatTime(record.joinTime)} PM +UTC</span>
            },
        }
    ]


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
                    tabType === 'content' ? loadTx(address).then() : loadInviteTx(address).then()
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
            if (!event.ids.includes(address)) return;
            if (event.source != 'user_spread') return;
            await loadStatFromDB(address);
        })
        load(inAddress).then();
        loadInviteTx(address).then();
    }, [])

    const load = async (address:string) => {
        const [ret, err] = await GetDIDAllRoleStat(address);
        setInviteStatList(ret);
        loadTx(address).then()
        await loadStatFromDB(address)
        await loadStatFromServer(address)
    }

    const loadTx = async (address:string) => {
        const request = new GetUserTxListByDIDRoleRequest();
        request.address = address;
        request.size = 10;
        request.startTxTime = (txList.length > 0 && txList[txList.length - 1].transferTime) || new Date().getTime();
        request.role = DIDRole.Transfer;
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

    const loadInviteTx = async (address:string) => {
        const request = new GetUserInviteTxListRequest();
        request.address = address;
        request.size = 10;
        request.startTxTime = (txInviteList.length > 0 && txInviteList[txInviteList.length - 1].joinTime) || new Date().getTime();
        const [ret, err] = await GetUserInviteTxList(request);
        loadMoreRef.current = ret.items.length >= 10;
        setLoadMore(true)
        if (err) {
            message.error(`Error: GetUserTxListByDIDRoleCreator is fail: ${err}`)
            return;
        }
        setNoMore(ret.items.length < 10)

        if (!txInviteList.length) {
            setTxInviteList(ret.items)
            return
        }
        setTxInviteList(prev => [...prev, ...ret.items]);
    }

    const loadStatFromServer = async (address:string) => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address = address;
        request.role = DIDRole.Transfer;
        await GetUserStatByDIDRole(request, 'user_spread');
    }

    const loadStatFromDB = async (address:string) => {
        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.Transfer));
        if (err) {
            return;
        }
        setStatList(ret)
    }
    const selectItemHandle = () => {
        setIsShowList(false);
        setTabType(tabType === 'content' ? 'group' : 'content');
        tabType === 'content' ? loadTx(address).then() : loadInviteTx(address).then();
    }
    return (
      <>
          <div className={styles.tableContainer}>
              <div className={styles.tabWrap}>
                  <div className={styles.tabItem} onClick={() => setIsShowList(!isShowList)}>
                      <img className={styles.tabIcon1} src={tabType === 'content' ? tabIcon1 : tabIcon2} alt="" />
                      <div className={styles.tabItemText}>{tabType === 'content' ? formatMessage({id: 'personal_dcirclescan_pop_content', defaultMessage: 'DID内容广播能力证明'})
                          : formatMessage({id: 'personal_dcirclescan_pop_group', defaultMessage: '群广播能力证明'})}</div>
                      <img className={styles.tabDown} src={tabDown} alt="" />
                  </div>
                  {
                      isShowList && <div className={styles.showDownList} onClick={selectItemHandle}>
                          <img className={styles.tabIcon2} src={tabType === 'content' ? tabIcon2 : tabIcon1} alt="" />
                          <div className={styles.tabItemText}>{tabType === 'content' ? formatMessage({id: 'personal_dcirclescan_pop_group', defaultMessage: '群广播能力证明'})
                               : formatMessage({id: 'personal_dcirclescan_pop_content', defaultMessage: 'DID内容广播能力证明'})}</div>
                      </div>
                  }
              </div>
              {
                  tabType === 'content' ?
                      <div className={classNames([styles.tableWrap, styles.fiveTableWrap, styles.firstTableWrap])}>
                        <Table rowKey="dataUpdateTime" scroll={{ x: true }} columns={columnsStat} dataSource={[statList]} pagination={false} />
                      </div>
                      : <div className={classNames([styles.tableWrap, styles.fiveTableWrap, styles.firstTableWrap])}>
                          <Table rowKey="dataUpdateTime" scroll={{ x: true }} columns={columnsStatInvite} dataSource={[inviteStatList.items[4]]} pagination={false} />
                      </div>
              }
              <div className={classNames([styles.tableWrap, styles.fiveTableWrap, styles.secondTableWrap])}>
                  {
                      tabType === 'content' ?
                          <Table rowKey="buyTime" scroll={{ x: true }} columns={columnsTx} dataSource={ txList } pagination={false} />
                          : <Table rowKey="buyTime" scroll={{ x: true }} columns={columnsInviteTx} dataSource={ txInviteList } pagination={false} />
                  }
              </div>
          </div>
          { !noMore && <Spin/> }
      </>
    )
}
