import styles from "./index.module.scss";
import {Image} from "antd";
import logo from "../../user/image/logo.png";
import {useNavigate} from "react-router-dom";
interface Props {
    address?: string
}
export default function Header(props: Props) {
    const navigate = useNavigate();
    const toIndex = () => {
        if(!props.address) return
    }
    return <div className={styles.headerWrap}>
        <div className={styles.logoWrap} onClick={toIndex}>
            <Image className={styles.logo} src={logo} preview={false}></Image>
        </div>
    </div>
}
