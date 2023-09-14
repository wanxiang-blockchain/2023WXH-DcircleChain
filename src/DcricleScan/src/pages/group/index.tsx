import styles from './index.module.scss';
import copyImg from "./image/copy.png";
import icon from "./image/icon.png";
import icon1 from "./image/icon1.png";
import noAvatar from "./image/noAvatar.png"
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import copy from "copy-to-clipboard";
import {message, Modal} from "antd";
import Cell from "../component/Cell";
import React, {useEffect, useState} from "react";
import {Async} from "../../Async";
import {AvatarInfo, GetGroupInfo} from "../../api/GetGroupInfo";
import CommonSkeleton from "../component/commonSkeleton";
import ContentList from "./components/ContentList";
import formatTime from "../../helper/formatTime";
import formatNum from "../../helper/formatNum";
import {FormattedMessage, useIntl} from "react-intl";
import {Panel} from "../component/echarts";
import {GetUserStatByDIDRole, GetUserStatByDIDRoleRequest} from "../../api/GetUserStatByDIDRole";
import {DIDRole} from "../../api/DIDRole";
import {statisticsItem} from "../user";
import LoadFail from "../component/loadFail";
import {handleQueryLinkAndSaveHttp, saveHttp} from "../../helper/handleUrl";
import Header from "../component/header";
import GroupGateway from "../component/groupGateway";
import AvatarBorder from "./components/AvatarBorder";
import {OssImage} from "../../oss/OssImage";
import {DBDIDArticleRole} from "../../db/DBDIDArticleRole";
import {GetDScan} from "../../db/db";
import {getUs} from "../../DIDBrowser";
import buildId = DBDIDArticleRole.buildId;
import DidData from "../component/didData";
interface GroupInfo {
    address: string;
    chatId: string;
    status: number;
    createTime: number;
    creatorAddress: string;
    memberNums: number;
    contentNums: number;
    name:string;
    avatar:AvatarInfo;
    nameOnChainTxId:string;
    avatarOnChainTxId:string;
    essenceKey:string;
    maxMemberNums: number;
    joinedTotalMemberNums: number;
}
interface DidItem {
    title: string;
    value: number;
}

export enum GroupTabType {
    consumptionTimes = '1',
    historyHighest = '2',
    i = '4'
}

export function Group() {
    const navigate = useNavigate();
    const location = useLocation();
    // 再存一次，防止刷新时丢失
    const state = useLocation().state;
    state && saveHttp(state.url);
    handleQueryLinkAndSaveHttp(location.search);

    const { groupAddress, language } = useParams();
    const [address] = useState<string>(groupAddress??"");
    const [groupInfo, setGroupInfo] = useState<GroupInfo>({} as GroupInfo);
    const [statisticsData, setStatisticsData] = useState<statisticsItem[]>([]);
    const [DIDAllRoleStat, setDIDAllRoleStat] = useState<DBDIDArticleRole.Document>({} as DBDIDArticleRole.Document);
    const [didData, setDidData] = useState<DidItem[]>([]);
    const [groupType, setGroupType] = useState<string>('');
    const [dataUpdateTime, setDataUpdateTime] = useState<number>(0);
    const [requestCount, setRequestCount] = useState<number>(0);
    const [open, setOpen] = useState<boolean>(false);

    const { formatMessage } = useIntl();
    const copyFun = async(e: MouseEvent, add:string|number) => {
        e.stopPropagation();
        copy(add + '');
        await message.success(formatMessage({id: 'copy_successfully'}));
    };

    useEffect(() => {
        Async(async () => {
            const [groupInfo, err] = await GetGroupInfo({address: address});
            if(err) {
                const count = requestCount + 1;
                setRequestCount(count);
            }
            getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
                if (!event.ids.includes(address)) return;
                if (event.source != 'group_index') return;
                await loadDIDAllRoleStat(address, groupInfo.maxMemberNums);
            })
            setGroupInfo(groupInfo);
            await loadDIDAllRoleStat(address, groupInfo.maxMemberNums);
            await loadDIDAllRoleStateFromServer(address);
        })
    }, [address, formatMessage]);

    const loadDIDAllRoleStateFromServer = async (address:string) => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address = address;
        request.role = DIDRole.SingleGroupStat;
        await GetUserStatByDIDRole(request, 'group_index');
    }

    const loadDIDAllRoleStat = async (address:string, maxMemberCount: number) => {
        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.SingleGroupStat));
        if (err) {
            return;
        }
        setDIDAllRoleStat(ret);
        const statisticsData = [{
            type: GroupTabType.i,
            flex: 'column',
            count: 0
        },{
            title: formatMessage({id: 'consumption_times'}),
            icon: 'icon-jiaoyipianshu',
            type: GroupTabType.consumptionTimes,
            flex: 'row',
            count: ret.cTimes
        },{
            title: formatMessage({id: 'group_hexagonal_data_history_highest_number'}),
            icon: 'icon-chuanboli1',
            type: GroupTabType.historyHighest,
            flex: 'row',
            count: maxMemberCount
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

    const buildContent = (content:string|number|string[], config: {color: string, copy: boolean, fn?: Function } = {color: '#000000', copy: false}):JSX.Element => {
        if (Array.isArray(content)) {
            return (
              <>
                  {
                      content.map((item, index) => (
                        <div key={index} style={{color: config.color}}>
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

    const toUser = (address:string) => {
        if (address === '-') return;
        navigate(`/user/${address}`)
    }

    if(requestCount === 1) {
        return <LoadFail />
    }
    const buildAvatar = (info: GroupInfo) => {
        return (
          <div className={styles.avatarWrap}>
              <OssImage
                style={{borderRadius: '8px', width: '64px', height: '54px', objectFit: 'cover'}}
                objectId={info.avatar.objectId} objectKey={info.avatar.key} />
          </div>
          );
    }

    const getAvatar = (info: GroupInfo) => {
        if (info.avatarOnChainTxId.length <= 0) {
            return <img className={styles.copyImg1} src={noAvatar} alt=""/>
        }
        if (info.avatar.objectId.length == 0 || info.avatar.key.length == 0) {
            return <img className={styles.copyImg1} src={noAvatar} alt=""/>
        }
        return buildAvatar(groupInfo);
    }

    const buildHeadInfo = (info: GroupInfo) => {
        return (
          <div className={styles.groupAvatarWrap}>
              <AvatarBorder uid={groupInfo.address} uidSource="group" height="54px" type="border64">
                  {getAvatar(info)}
              </AvatarBorder>
          </div>
        );
    }

    const buildHeadTitle = (info: GroupInfo) => {
        if (info.nameOnChainTxId.length <= 0) {
            return (<div className={styles.groupHeaderImgTitle1}>
                <FormattedMessage id="dcircle_scan_group_no_name" defaultMessage="暂无链上群名称"/>
            </div>);
        }
        return (<div className={styles.groupHeaderImgTitle}>{info.name}</div>);
    }

    const openHandle = (type: string) => {
        setOpen(true);
        setGroupType(type);
        const didData = type === GroupTabType.consumptionTimes ? [{
            title: formatMessage({id: 'consumption_times'}),
            value: DIDAllRoleStat.cTimes
        },{
            title: formatMessage({id: 'total_content'}),
            value: DIDAllRoleStat.nNums
        },{
            title: formatMessage({id: 'message_consumers'}),
            value: DIDAllRoleStat.cNums
        }] : [{
            title: formatMessage({id: 'group_hexagonal_data_history_highest_number'}),
            value: groupInfo.maxMemberNums
        },{
            title: formatMessage({id: 'group_hexagonal_data_history_highest_number_current_number'}),
            value: groupInfo.memberNums
        },{
            title: formatMessage({id: 'group_hexagonal_data_history_highest_number_join_group_total_number'}),
            value: groupInfo.joinedTotalMemberNums
        }]
        setDidData(didData);
    }

    return (
        <>
            {!groupInfo.creatorAddress ? <CommonSkeleton/> :
                <div className={styles.container}>
                    <Header address={address} />
                    <div className={styles.topWrap}>
                        <div className={styles.groupHeaderImgWrap}>
                            {
                                buildHeadInfo(groupInfo)
                            }
                            {
                                buildHeadTitle(groupInfo)
                            }

                        </div>
                        <div className={styles.titleWrap}>
                            <div className={styles.title}><FormattedMessage id="group_address" defaultMessage="群地址" /></div>
                            <div className={styles.content}>
                                <span>{address}</span>
                                <img className={styles.copyImg} src={copyImg} alt="" onClick={(e: any) => copyFun(e, address)}/>
                            </div>
                        </div>
                        <div className={styles.mainWrap}>
                            <Panel setActiveBtn={openHandle} dataUpdateTime={dataUpdateTime} statisticsData={statisticsData} />
                            <div className={styles.overviewWrap}>
                                <div className={styles.title}><FormattedMessage id="overview" /></div>
                                <div className={styles.infoWrap}>
                                    <Cell title={`${formatMessage({id: 'owner'})}：`}
                                          content={
                                              buildContent(formatNum(groupInfo.creatorAddress),
                                                  {
                                                      color: '#3470E9',
                                                      copy: formatNum(groupInfo.creatorAddress) !== '-',
                                                      fn: () => toUser(formatNum(groupInfo.creatorAddress) + '')
                                                  }
                                              )
                                          }></Cell>
                                    <Cell title={`${formatMessage({id:'group_size'})}`} content={formatNum(groupInfo.memberNums)}></Cell>
                                    <Cell title={`${formatMessage({id:'state'})}：`} content={formatMessage({id: groupInfo.status === -1 ? 'invalid' : 'efficient'})}></Cell>
                                    <Cell title={`${formatMessage({id:'creation_time'})}：`} content={formatTime(groupInfo.createTime)}></Cell>
                                </div>
                            </div>
                        </div>
                    </div>
                    <GroupGateway address={address} essenceKey={groupInfo.essenceKey} />
                    <ContentList address={address} />
                    <Modal
                        centered
                        closable={false}
                        maskClosable={true}
                        open={open}
                        footer={null}>
                        <div className={styles.modalWrap}>
                            <div className={styles.modalMainWrap}>
                                <div className={styles.titleWrap}>
                                    <img className={styles.icon} src={groupType === GroupTabType.consumptionTimes ? icon : icon1} alt=""/>
                                    <div className={styles.modalTitle}>
                                        {groupType === GroupTabType.consumptionTimes ?
                                            <FormattedMessage id="consumption_times" />: <FormattedMessage id="group_hexagonal_data_history_highest_number" />}
                                    </div>
                                </div>
                                <div className={styles.tips1}>
                                    {groupType === GroupTabType.consumptionTimes && <FormattedMessage id="group_hexagonal_data_transactions_illustrate" />}
                                </div>
                                <div className={styles.tips2}>
                                    {groupType === GroupTabType.consumptionTimes ? <FormattedMessage id="group_hexagonal_data_transactions_illustrate2" /> :
                                        <FormattedMessage id="group_hexagonal_data_history_highest_number_illustrate" />}
                                </div>
                                <DidData data={didData}></DidData>
                            </div>
                            <div className={styles.known} onClick={() => setOpen(false)}><FormattedMessage id="picture_know" /></div>
                        </div>
                    </Modal>
                </div>
            }
        </>
    )
}
