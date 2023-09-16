import { Progress, Space } from 'antd';
import styles from './index.module.scss'
interface Props {
  percent: number
  size: number
}

const Index = ({ size = 20, percent = 0 }: Props): JSX.Element => {
  return (
    percent === 0 || percent >= 100 ? <></> : <div className={styles.progressBar}>
      <Space direction="vertical">
        <Progress
          type="circle"
          percent={percent}
          size={size}
          showInfo={false}
          strokeWidth={10}
          trailColor={'rgba(50,50,51.5)'}
          strokeColor={'#fff'}/>
      </Space>
    </div>
  );
};

export default Index;
