import styles from './index.module.scss';
import copyImg from "./image/copy.png";
import statusImg from "./image/status.png";
import {useLocation, useParams} from 'react-router-dom';
import copy from "copy-to-clipboard";
import {message, Divider} from "antd";
import Cell from "../component/Cell";
import React, {useEffect, useState} from "react";
import {Async} from "../../Async";
import {GetTxInfo, GetTxInfoResponse} from "../../api/GetTxInfo";
import formatTime from "../../helper/formatTime";
import formatNum from "../../helper/formatNum";
import {useNavigate} from "react-router-dom";
import {useIntl} from "react-intl";
import LoadFail from "../component/loadFail";
import {handleQueryLinkAndSaveHttp, saveHttp} from "../../helper/handleUrl";
import Header from "../component/header";

export function Transaction() {
    const navigate = useNavigate();
    const location = useLocation();
    // 再存一次，防止刷新时丢失
    const state = useLocation().state;
    state && saveHttp(state.url);
    handleQueryLinkAndSaveHttp(location.search);

    const { transactionHash, language } = useParams();
    const [address, setAddress] = useState<string>(transactionHash??"");
    const [txtInfo, setTxtInfo] = useState<GetTxInfoResponse>(new GetTxInfoResponse());
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

    const toContent = (address:string) => {
        if (address === '-') return;
        navigate(`/article/${address}`)
    }

    const buildStatusContent = (content:string, color: string = '#000000'):JSX.Element => {
        return <div className={styles.statusWrap}>
            <img className={styles.statusImg} src={statusImg} alt="" onClick={(e: any) => copyFun(e, content)}/>
            <span style={{color: color}}>{content}</span>
        </div>
    }
    useEffect(() => {
        Async(async () => {
            const [ret, err] = await GetTxInfo({id: address});
            if (err) {
                const count = requestCount + 1;
                setRequestCount(count);
                message.error(`Error: GetTxtInfo is fail: ${err}`)
                return;
            }
            setTxtInfo(ret)
        })
        let dom = document.getElementById('containerTransation')
        // dom && dom.scrollTop = 0;
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
                <Cell type="translation" title={`${formatMessage({id: 'consumer'})}：`} content={buildContent(txtInfo.buyUserid, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.buyUserid)})}></Cell>
                <div className={styles.signData}>{ `Signature：${formatNum(txtInfo.buySignData)}` }</div>
                <Cell type="translation" title={`${formatMessage({id: 'content'})}：`} content={buildContent(txtInfo.didAddress, {color: '#3470E9', copy: true, fn: () => toContent(txtInfo.didAddress)})}></Cell>
                <Cell type="translation" title={`${formatMessage({id: 'creator'})}：`} content={buildContent(txtInfo.creatorAddress, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.creatorAddress)})}></Cell>
                <div className={styles.signData}>{ `Signature：${formatNum(txtInfo.createSignData)}` }</div>
                <Cell type="translation" title={`${formatMessage({id: 'transaction_time'})}：`} content={buildContent(formatTime(txtInfo.txTime), {color: '#000000', copy: false})}></Cell>
                <Divider />
                <Cell type="translation" title={`${formatMessage({id: 'source_group'})}：`} content={buildContent(txtInfo.sourceChatId, {color: '#3470E9', copy: txtInfo.sourceChatId !== '-', fn: () => toGroup(txtInfo.sourceChatId)})}></Cell>
                <Cell type="translation" title={`${formatMessage({id: 'disseminator'})}：`} content={buildContent(txtInfo.transferId, {color: '#3470E9', copy: true, fn: () => toUser(txtInfo.transferId)})}></Cell>
                <div className={styles.signData} style={{display: "inline-block"}}>
                    <div>Txn Hash：</div>
                    <div>{ formatNum(txtInfo.transferSignData) }</div>
                </div>
                <Divider />
                <Cell type="translation" title="流通量：" content={buildContent(txtInfo.mNums, {color: '#000000', copy: false})}></Cell>
                <Cell type="translation" title="Nonce:" content={buildContent(txtInfo.nonce, {color: '#000000', copy: false})}></Cell>
            </div>
        </div>
    </div>
    )
}
