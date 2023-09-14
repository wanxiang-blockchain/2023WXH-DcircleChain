import styles from "./index.module.scss";
import loadingImg from "../../image/loading.png";
export default function Spin() {
    return <div className={styles.spin}><img className={styles.loadingImg} src={loadingImg} alt="" /></div>
}
