import styles from "./index.module.scss";
import failImg from "./image/failImg.png";
import {FormattedMessage} from "react-intl";
export default function LoadFail() {
    const handleReload = () => {
        window.location.reload();
    };
    return <div className={styles.failWrap}>
        <img className={styles.failImg} src={failImg} alt="" />
        <div className={styles.tips}><FormattedMessage id="dcirclescan_nonetwork" defaultMessage="Failed to communicate with node, please try again later" /></div>
        <div className={styles.failBtn} onClick={handleReload}><FormattedMessage id="dcirclescan_button_reload" defaultMessage="Reload" /></div>
    </div>
}
