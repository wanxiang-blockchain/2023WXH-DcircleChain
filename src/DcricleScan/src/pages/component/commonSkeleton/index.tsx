import styles from "./index.module.scss";
import { Skeleton } from 'antd';
export default function CommonSkeleton() {
    return <div className={styles.skeletonWrap}><Skeleton active paragraph={{ rows: 6 }} /></div>
}
