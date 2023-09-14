import styles from './index.module.scss';
import copyImg from "./image/copy.png";
import {useParams, useLocation} from 'react-router-dom';
import copy from "copy-to-clipboard";
import {message} from "antd";
import React, {useEffect, useRef, useState} from "react";
import {Async} from "../../Async";
import classNames from 'classnames';
import Create from "./component/Create";
import Group from "./component/Group";
import Spread from "./component/Spread";
import Consume from "./component/Consume";
import UserModal from "./component/UserModal";
import CommonSkeleton from "../component/commonSkeleton";
import { Panel } from "../component/echarts";
import LoadFail from "../component/loadFail";
import Header from "../component/header";
import {GetDIDAllRoleStat, GetDIDAllRoleStatRequestResponse} from "../../api/GetDIDAllRoleStat";
import {
    GetUserStatByDIDRole,
    GetUserStatByDIDRoleRequest,
    GetUserStatByDIDRoleResponse
} from "../../api/GetUserStatByDIDRole";
import { FormattedMessage, useIntl } from 'react-intl';
import {handleQueryLinkAndSaveHttp, saveHttp} from "../../helper/handleUrl";
import {DBDIDArticleRole} from "../../db/DBDIDArticleRole";
import {GetDScan} from "../../db/db";
import {getUs} from "../../DIDBrowser";
import {DIDRole} from "../../api/DIDRole";
import buildId = DBDIDArticleRole.buildId;

export enum UserTabType {
    Group = '0',
    Create = '1',
    Spread = '2',
    Consume = '3',
    i = '4'
}

export interface statisticsItem {
    title?: string;
    icon?: string;
    type: string;
    count: number;
    flex: string;
}

interface DidItem {
    title: string;
    value: number;
}

interface DidObj {
    title: string;
    tips1: string;
    tips2: string;
}

interface TitleObj {
    [UserTabType.Group]: DidObj;
    [UserTabType.Create]: DidObj;
    [UserTabType.Spread]: DidObj;
    [UserTabType.Consume]: DidObj;
}

const cacheTabs =  new Map<UserTabType, JSX.Element>([])

export function User() {
    const { userAddress, tab } = useParams();
    const location = useLocation();
    // 再存一次，防止刷新时丢失
    const state = useLocation().state;
    state && saveHttp(state.url);

    handleQueryLinkAndSaveHttp(location.search);

    const currentAddress = useRef<string>(userAddress??"")
    const [address, setAddress] = useState<string>(userAddress??"");
    const [activeBtn, setActiveBtn] = useState<UserTabType>(UserTabType.Create);
    const [userInfo, setUserInfo] = useState<GetUserStatByDIDRoleResponse | null>(null);
    const [statisticsData, setStatisticsData] = useState<statisticsItem[]>([]);
    const [dataUpdateTime, setDataUpdateTime] = useState<number>(0);
    const [requestCount, setRequestCount] = useState<number>(0);
    const [didData, setDidData] = useState<DidItem[]>([]);
    const [didObj, setDidObj] = useState<DidObj>({} as DidObj);
    const [open, setOpen] = useState<boolean>(false);
    const [DIDAllRoleStat, setDIDAllRoleStat] = useState<GetDIDAllRoleStatRequestResponse>({} as GetDIDAllRoleStatRequestResponse);
    const { formatMessage } = useIntl();
    const copyFun = async() => {
        copy(address);
        await message.success(formatMessage({id: 'copy_successfully'}));
    };

    useEffect(() => {
        if (localStorage.getItem('info')) {
            const obj = JSON.parse(localStorage.getItem('info')!)
            if (obj.uid !== userAddress) {
                setActiveBtn(UserTabType.Create);
                return
            }
            const active: UserTabType = obj.tab
            setActiveBtn(active);
            return;
        }
        if (tab) {
            setActiveBtn(tab as UserTabType);
            const obj:{tab: UserTabType, uid:string} = {
                tab: tab as UserTabType,
                uid: userAddress!
            }
            localStorage.setItem('info', JSON.stringify(obj));
            return;
        }
    }, [])

    useEffect(() => {
        Async(async () => {
            loadDIDAllRoleStat(address).then();
            await loadFromDB(address);
        })
    }, [address, formatMessage]);

    useEffect(() => {
        Async(async () => {
            getUs().nc.addEvent(DBDIDArticleRole.ChangedEvent, async (event) => {
                if (!event.ids.includes(currentAddress.current)) return;
                if (event.source != 'user_update') return;
                await loadFromDB(currentAddress.current, true);
            })
            loadDIDAllRoleStat(address).then();
            await loadFromDB(address);
            await loadFromServer(address);
        })
    }, [])

    const loadFromServer = async (address:string) => {
        const request = new GetUserStatByDIDRoleRequest();
        request.address =  address;
        await GetUserStatByDIDRole(request, 'user_update');
    }

    const loadFromDB = async (address:string, post = false) => {
        const [ret, err] = await DBDIDArticleRole.FindById(GetDScan(), buildId(address, DIDRole.Creator));
        if (err) {
            if (!post) return;
            const count = requestCount + 1;
            setRequestCount(count);
            return;
        }

        setUserInfo(ret);
        const dom = document.getElementById('containerUser')
        dom && dom.scrollIntoView({ behavior: "smooth", block: "start" })
    }
    const loadDIDAllRoleStat = async (address:string) => {
        const [ret, err] = await GetDIDAllRoleStat(address);
        setDIDAllRoleStat(ret);
        const did_data_organization = formatMessage({id: 'did_data_organization', defaultMessage: '组织力'});
        const statisticsData = [{
            title: did_data_organization,
            icon: 'icon-chuangzaoli',
            type: UserTabType.Group,
            flex: 'column',
            count: ret.items[0].cTimes
        },{
            title: formatMessage({id: 'did_data_creativity', defaultMessage: '创造力'}),
            icon: 'icon-chuanboli1',
            type: UserTabType.Create,
            flex: 'row',
            count: ret.items[1].cTimes
        },{
            title: formatMessage({id: 'did_data_communication_power', defaultMessage: '传播力'}),
            icon: 'icon-chuanboli',
            type: UserTabType.Spread,
            flex: 'row',
            count: ret.items[3].cTimes + ret.items[4].joinTimes
        },{
            title: formatMessage({id: 'did_data_consumption_power', defaultMessage: '消费力'}),
            icon: 'icon-xiaofeili',
            type: UserTabType.Consume,
            flex: 'column',
            count: ret.items[2].cTimes
        },{
            type: UserTabType.i,
            flex: 'column',
            count: 1
        }]
        setStatisticsData(statisticsData);
        const dataUpdateTime = ret.items[0].dataUpdateTime || ret.items[1].dataUpdateTime || ret.items[2].dataUpdateTime || ret.items[3].dataUpdateTime
        setDataUpdateTime(dataUpdateTime);
        if (err) {
            const count = requestCount + 1;
            setRequestCount(count);
            message.error(`Error: GetDIDArticleInfo is fail: ${err}`)
            return;
        }
    }
    const btnArr = [
        {
            title: formatMessage({id: 'did_data_creativity', defaultMessage: '创造力'}),
            key: UserTabType.Create
        },{
            title: formatMessage({id: 'did_data_organization', defaultMessage: '组织力'}),
            key: UserTabType.Group
        },{
            title: formatMessage({id: 'did_data_communication_power', defaultMessage: '传播力'}),
            key: UserTabType.Spread
        },{
            title: formatMessage({id: 'did_data_consumption_power', defaultMessage: '消费力'}),
            key: UserTabType.Consume
        }
    ]

    const updateInfo = (address:string) => {
        Async(async () => {
            setAddress(address)
            currentAddress.current = address;
            loadDIDAllRoleStat(address).then();
            loadFromServer(address).then();
        })
    }

    const buildTab = (address:string) => {
        if (cacheTabs.has(activeBtn)) {
            return cacheTabs.get(activeBtn)
        }
        const buildCell = () => {
            if (activeBtn === UserTabType.Create) {
                return <Create address={address} upddateInfo={updateInfo} />;
            }
            if (activeBtn === UserTabType.Group) {
                return <Group address={address} upddateInfo={updateInfo} />;
            }
            if (activeBtn === UserTabType.Spread) {
                return <Spread address={address}  updateInfo={updateInfo} />;
            }
            return <Consume address={address} upddateInfo={updateInfo} />;
        }
        const cell = buildCell();
        cacheTabs.set(activeBtn, cell)
        return cell;
    }
    if(requestCount === 1) {
        return <LoadFail />
    }
    const openHandle = (type: UserTabType.Group | UserTabType.Create | UserTabType.Spread | UserTabType.Consume) => {
        setOpen(true);
        setActiveBtn(type)
        const group = {
            title: formatMessage({id: 'did_data_organization'}),
            tips1: formatMessage({id: 'group_hexagonal_data_history_highest_number'}),
            tips2: formatMessage({id: 'did_hexagon_organization_introduce'})
        }
        const create = {
            title: formatMessage({id: 'did_data_creativity'}),
            tips1: formatMessage({id: 'did_hexagon_creativity'}),
            tips2: formatMessage({id: 'did_hexagon_creativity_introduce'})
        }
        const spread = {
            title: formatMessage({id: 'did_data_communication_power'}),
            tips1: formatMessage({id: 'did_hexagon_communication'}),
            tips2: formatMessage({id: 'did_hexagon_communication_introduce'})
        }
        const consume = {
            title: formatMessage({id: 'did_data_consumption_power'}),
            tips1: formatMessage({id: 'did_hexagon_consumption'}),
            tips2: formatMessage({id: 'did_hexagon_consumption_introduce'})
        }
        const data0 = [{
            title: formatMessage({id: 'consumption_times'}),
            value: DIDAllRoleStat.items[0].cTimes
        },{
            title: formatMessage({id: 'communication_group'}),
            value: DIDAllRoleStat.items[0].gNums
        },{
            title: formatMessage({id: 'message_consumers'}),
            value: DIDAllRoleStat.items[0].cNums
        },{
            title: formatMessage({id: 'number_dissemination_articles'}),
            value: DIDAllRoleStat.items[0].nNums
        }];
        const data1 = [{
            title: formatMessage({id: 'consumption_times'}),
            value: DIDAllRoleStat.items[1].cTimes
        },{
            title: formatMessage({id: 'total_content'}),
            value: DIDAllRoleStat.items[1].nNums
        },{
            title: formatMessage({id: 'message_consumers'}),
            value: DIDAllRoleStat.items[1].cNums
        },{
            title: formatMessage({id: 'disseminator_member'}),
            value: DIDAllRoleStat.items[1].tNums
        },{
            title: formatMessage({id: 'communication_group'}),
            value: DIDAllRoleStat.items[1].gNums
        }];
        const data2 = [{
            title: formatMessage({id: 'personal_did_pob_did_transactions_number'}),
            value: DIDAllRoleStat.items[3].cTimes
        },{
            title: formatMessage({id: 'communication_group'}),
            value: DIDAllRoleStat.items[3].gNums
        },{
            title: formatMessage({id: 'message_consumers'}),
            value: DIDAllRoleStat.items[3].cNums
        },{
            title: formatMessage({id: 'number_dissemination_articles'}),
            value: DIDAllRoleStat.items[3].nNums
        },{
            title: formatMessage({id: 'personal_did_pob_did_enter_group_times'}),
            value: DIDAllRoleStat.items[4].joinUserCount
        },{
            title: formatMessage({id: 'personal_did_proof_broadcasting_power_enter_people'}),
            value: DIDAllRoleStat.items[4].joinTimes
        },{
            title: formatMessage({id: 'personal_did_proof_broadcasting_power_share_group'}),
            value: DIDAllRoleStat.items[4].shareGroupCount
        }];
        const data3 = [{
            title: formatMessage({id: 'consumption_times'}),
            value: DIDAllRoleStat.items[2].cTimes
        },{
            title: formatMessage({id: 'total_content'}),
            value: DIDAllRoleStat.items[2].nNums
        },{
            title: formatMessage({id: 'communication_group'}),
            value: DIDAllRoleStat.items[2].gNums
        }];

        const didData: DidItem[] =  eval(`data${type}`) || [];
        const titleObj: TitleObj = {
            [UserTabType.Group]: group,
            [UserTabType.Create]: create,
            [UserTabType.Spread]: spread,
            [UserTabType.Consume]: consume
        }
        setDidObj(titleObj[type]);
        setDidData(didData);
    }
    return (
    <>
        {!userInfo ? <CommonSkeleton /> :
            <div className={styles.container} id={'containerUser'}>
                <Header />
                <div className={styles.titleWrap}>
                    <div className={styles.title}>
                        <FormattedMessage id="user_address" />
                    </div>
                    <div className={styles.content}>
                        <span>{address}</span>
                        <img className={styles.copyImg} src={copyImg} alt="" onClick={copyFun}/>
                    </div>
                </div>
                <Panel setActiveBtn={openHandle} dataUpdateTime={dataUpdateTime} statisticsData={statisticsData} />
                <div className={styles.btnWrap}>
                    {btnArr.map((item, index) => (
                      <div
                        key={item.key}
                        className={classNames([styles.btn, activeBtn === item.key && styles.activeBtn])}
                        onClick={() => {
                            setActiveBtn(item.key)
                            const obj:{tab: UserTabType, uid:string} = {
                                tab: item.key as UserTabType,
                                uid: userAddress!
                            }
                            localStorage.setItem('info', JSON.stringify(obj));
                        }}>
                        {item.title}
                      </div>))}
                </div>
                { buildTab(address) }
                <UserModal open={open} close={setOpen} type={activeBtn} didData={didData} didObj={didObj}></UserModal>
            </div>
        }
    </>
    )
}
