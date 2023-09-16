import styles from "./index.module.scss";
import classNames from "classnames";


interface Props {
    type?: string;
    title: string;
    content: JSX.Element | string | number;
    blue?: boolean;
}

/*
* 概述内容
* **/
export default function Cell({type, title, content, blue}: Props) {
    return (
        <div className={classNames(styles.infoItem, type === 'translation' && styles.translationInfoItem)}>
            <div className={styles.infoTitle}>{title}</div>
            <div className={styles.infoContent}>{content}</div>
        </div>
    )
}
