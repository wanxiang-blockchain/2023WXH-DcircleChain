import styles from './index.module.scss';
import copyImg from "./image/copy.png";
import statusImg from "./image/status.png";
import {useLocation, useParams} from 'react-router-dom';
import copy from "copy-to-clipboard";
import {message} from "antd";
import Cell from "../component/Cell";
import React, {useEffect, useState} from "react";
import {Async} from "../../Async";
import {GetUserInviteTxInfo, GetUserInviteTxInfoResponse} from "../../api/GetUserInviteTxInfo";
import formatTime from "../../helper/formatTime";
import formatNum from "../../helper/formatNum";
import {useNavigate} from "react-router-dom";
import {useIntl} from "react-intl";
import LoadFail from "../component/loadFail";
import {handleQueryLinkAndSaveHttp, saveHttp} from "../../helper/handleUrl";
import Header from "../component/header";

export function TransactionGroup() {
    const navigate = useNavigate();
    const location = useLocation();
    // 再存一次，防止刷新时丢失
    const state = useLocation().state;
    state && saveHttp(state.url);
    handleQueryLinkAndSaveHttp(location.search);

    const { transactionHash, language } = useParams();
    const [address, setAddress] = useState<string>(transactionHash??"");
    const [txtInfo, setTxtInfo] = useState<GetUserInviteTxInfoResponse>(new GetUserInviteTxInfoResponse());
    const [requestCount, setRequestCount] = useState<number>(0);

    const { formatMessage } = useIntl();

    const copyFun = async(e: MouseEvent, add:string|number) => {
        e.stopPropagation();
        copy(add + '');
        await message.success(formatMessage({id: 'copy_successfully'}));
    };
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

    const toUser = (address:string) => {
        if (address === '-') return;
        navigate(`/user/${address}`)
    }

    const toGroup = (address:string) => {
        if (address === '-') return;
        navigate(`/group/${address}`)
    }

    const buildStatusContent = (content:string, color: string = '#000000'):JSX.Element => {
        return <div className={styles.statusWrap}>
            <img className={styles.statusImg} src={statusImg} alt="" onClick={(e: any) => copyFun(e, content)}/>
            <span style={{color: color}}>{content}</span>
        </div>
    }
    useEffect(() => {
        Async(async () => {
            const [ret, err] = await GetUserInviteTxInfo({id: address});
            if (err) {
                const count = requestCount + 1;
                setRequestCount(count);
                message.error(`Error: GetTxtInfo is fail: ${err}`)
                return;
            }
            setTxtInfo(ret)
        })
    }, []);

    if(requestCount === 1) {
        return <LoadFail />
    }

    return (
    <div className={styles.container} id={"containerTransation"}>
        <Header address={address} />
        <div className={styles.infoItem}>
            <div className={styles.infoTitle}>{`${formatMessage({id: 'txn_hash'})}：`}</div>
            <div className={styles.infoContent}>{buildContent(txtInfo.txHash, {color: '#000000', copy: true})}</div>
        </div>
        <div className={styles.overviewWrap}>
            <div className={styles.infoWrap}>
                <Cell type="translation" title={`${formatMessage({id: 'state'})}：`} content={buildStatusContent('Success', '#73B224')}></Cell>
                <Cell type="translation" title={`${formatMessage({id: 'owner'})}：`} content={buildContent(txtInfo.chatCreator, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.chatCreator)})}></Cell>
                <Cell type="translation" title={`${formatMessage({id: 'personal_dcirclescan_pop_group_shared_group'})}：`} content={buildContent(txtInfo.joinChatId, {color: '#3470E9', copy: true, fn: () => toGroup(txtInfo.joinChatId)})}></Cell>
                <div className={styles.signData}>{ `Signature：${txtInfo.inviteCodeTxHash}` }</div>
                <Cell type="translation" title={`${formatMessage({id: 'personal_dcirclescan_pop_group_hash_invite_people'})}：`} content={buildContent(txtInfo.invitorUid, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.invitorUid)})}></Cell>
                <div className={styles.signData}>{ `Signature：${txtInfo.inviteCodeTxHash}` }</div>
                <Cell type="translation" title={`${formatMessage({id: 'personal_dcirclescan_pop_group_Invitees'})}：`} content={buildContent(txtInfo.inviteeUid, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.inviteeUid)})}></Cell>
                <div className={styles.signData}>{ `Signature：-` }</div>
                <Cell type="translation" title={`${formatMessage({id: 'personal_dcirclescan_pop_group_hash_join_time'})}：`} content={buildContent(formatTime(txtInfo.joinTime), {color: '#000000', copy: false})}></Cell>
            </div>
        </div>
    </div>
    )
}
