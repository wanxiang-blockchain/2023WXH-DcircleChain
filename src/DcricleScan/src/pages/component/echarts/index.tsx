import React, {useEffect, useRef, useState} from 'react';
import * as echarts from 'echarts';
import styles from './index.module.scss';
import {statisticsItem, UserTabType} from '../../user';
import classNames from "classnames";
import '../../iconfont/iconfont.css';
import formatTime from "../../../helper/formatTime";
import {GroupTabType} from "../../group";

interface Props {
    dataUpdateTime: number;
    setActiveBtn?: Function;
    statisticsData: statisticsItem[];
}
const ChartTag = React.memo((props: { chartRef: any}) => {
    return <div ref={props.chartRef} style={{ width: '201px', height: '168px' }}></div>
})
export const Panel = (props: Props) => {
    const chartRef = useRef(null);
    const [sixStatisticsData, setSixStatisticsData] = useState<statisticsItem[] | null[]>([null, null, null, null, null, null]);

    const colorData = [
        { threshold: 0 },
        { threshold: 1 },
        { threshold: 10 },
        { threshold: 50 },
        { threshold: 200 },
        { threshold: 500 },
        { threshold: 1000 }
    ];
    function extendArray(data: any) {
        const result = [null, null, null, null, null, null];
        for (let i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }
    // 根据count在colorData中匹配出对应的最大值，超出1000，最大为1000
    function getMaxValue(count: number) {
        if(count === 0) return [10, 0];
        let value = 1000;
        let index = 0;
        for (let i = 0; i < colorData.length; i++) {
            const { threshold } = colorData[i];

            if (count >= threshold) {
                value = colorData[i + 1] && colorData[i + 1].threshold || 1000;
                index = i;
            } else {
                break;
            }
        }
        return [value, index];
    }
    function getFormatValue(count: number) {
        const [maxValue, index] = getMaxValue(count);
        const result = count/maxValue * (maxValue/6) + index * maxValue/6;
        return result > 1000 ? 1000 : result;
    }
    useEffect(() => {
        if(props.statisticsData.length === 0) return
        const count1 = props.statisticsData[0] && props.statisticsData[0].count || 0;
        const count2 = props.statisticsData[1] && props.statisticsData[1].count || 0;
        const count3 = props.statisticsData[2] && props.statisticsData[2].count || 0;
        const count4 = props.statisticsData[3] && props.statisticsData[3].count || 0;
        const count5 = props.statisticsData[4] && props.statisticsData[4].count || 0;
        // 第二个值为0，第一个就不依据此作图，单独画点，i点作区域（解决第二个为0时，第一个有值区域有问题）
        const firstValue = getFormatValue(count2) === 0 ? 0 : getFormatValue(count1);
        const cTimes = [getFormatValue(count2), firstValue, 0, getFormatValue(count5), getFormatValue(count4), getFormatValue(count3)];
        // @ts-ignore
        const chartInstance = echarts.init(chartRef.current);
        const sixData = extendArray(props.statisticsData);
        setSixStatisticsData(sixData);
        let colorStops: { offset: number, color: string }[] = [];
        const {countData, xOffset, yOffset, rOffset} = getCircleGradientData();
        countData.map(item => colorStops.push({ offset: item.offset, color: item.count === 0 ? 'rgba(238,238,238,0.3)' : getColorByData(item.count, 'opacity') }))
        // 在此处配置图表选项和数据
        const options = {
            backgroundColor: '#FFFFFF',
            radar: {
                name: {
                    textStyle: {
                        color: '#969799',
                        fontSize: 14,
                    },
                },
                shape: 'polygon',
                center: ['50%', '50%'],
                radius: '100%',
                startAngle: 120,
                scale: true,
                axisLine: {
                    lineStyle: {
                        color: 'rgba(219, 219, 219, 0.2)',
                        type: 'dashed'
                    },
                },
                splitLine: {
                    show: true,
                    lineStyle: {
                        width: 1,
                        type: 'dashed', // 设置为虚线
                        color: 'rgba(219, 219, 219, 0.3)',
                    },
                },
                indicator: [
                    { name: '', max: getMaxValue(count2)[0] },
                    { name: '', max: getMaxValue(count1)[0] },
                    { name: '', max: 100 },
                    { name: '', max: getMaxValue(count5)[0] },
                    { name: '', max: getMaxValue(count4)[0] },
                    { name: '', max: getMaxValue(count3)[0] }
                ],
                splitNumber: 3, // 控制网格
                splitArea: {
                    show: false
                },
            },
            grid: {
                position: 'center',
            },
            polar: {
                center: ['50%', '50%'], // 默认全局居中
                radius: '0%',

            },
            angleAxis: {
                min: 0,
                interval: 5,
                clockwise: false,
                axisTick: {
                    show: false,
                },
                axisLabel: {
                    show: false,
                },
                axisLine: {
                    show: false,
                },
                splitLine: {
                    show: false,
                },
            },
            radiusAxis: {
                min: 0,
                interval: 20,
                splitLine: {
                    show: false,
                },
            },
            series: getSeries()
        };
        // 使用配置项初始化图表
        chartInstance.setOption(options);

        // 在组件卸载时销毁图表
        return () => {
            chartInstance.dispose();
        };
        function getSeries() {
            let series = [
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle', // 拐点的样式，还可以取值'rect','angle'等
                    symbolSize: 3, // 拐点的大小
                    animation: false,
                    itemStyle: {
                        normal: {
                            color: 'transparent'
                        },
                    },
                    areaStyle: {
                        color: {
                            type: 'radial',
                            x: xOffset, // 圆心 x 坐标
                            y: yOffset,
                            r: rOffset,
                            colorStops: colorStops
                        }

                    },
                    lineStyle: {
                        width: 0,
                        color: '#05D5FF',
                    },
                    data: [
                        {
                            value: cTimes
                        }
                    ],
                },
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        width: 0
                    },
                    itemStyle: {
                        normal: {
                            color: getColorByData(count1)
                        },
                    },
                    data: [{
                        value: [0, getFormatValue(count1)]
                    }],
                },
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle',
                    symbolSize: 4,
                    itemStyle: {
                        normal: {
                            color: getColorByData(count2)
                        },
                    },
                    data: [{
                        value: [getFormatValue(count2)]
                    }],
                },
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        width: 0
                    },
                    itemStyle: {
                        normal: {
                            color: getColorByData(count3)
                        },
                    },
                    data: [{
                        value: [0, 0, 0, 0, 0, getFormatValue(count3)]
                    }],
                },
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        width: 0
                    },
                    itemStyle: {
                        normal: {
                            color: getColorByData(count4)
                        },
                    },
                    data: [{
                        value: [0, 0, 0, 0, getFormatValue(count4), 0]
                    }],
                },
                {
                    name: '',
                    type: 'radar',
                    symbol: 'circle', // 拐点的样式，还可以取值'rect','angle'等
                    symbolSize: 3, // 拐点的大小
                    animation: false,
                    itemStyle: {
                        normal: {
                            color: 'transparent'
                        },
                    },
                    areaStyle: {
                        color: {
                            type: 'linear',
                            x: 0,
                            y: 1,
                            x2: 0,
                            y2: 0,
                            colorStops: [{ offset: 0, color: 'rgba(255,255,255,1)' }, { offset: 0.7, color: 'rgba(255,255,255,0.8)' }, { offset: 1, color: 'rgba(255,255,255,0.2)' }]
                        }
                    },
                    z: 99,
                    lineStyle: {
                        width: 0
                    },
                    data: [{
                        value: [0, 0, 0, getFormatValue(count5), getFormatValue(count4), 0]
                    }],
                }
            ]
            if(count2 === 0 && count1 !== 0) {
                //画I点区域
                series.push(...getIArea([1.8, getFormatValue(count1), 0, 0, 0, 0]));
                //画I点
                series.push(...getIPoints([1.8, 0, 0, 0, 0, 0]));
            }
            if(count3 === 0 && count2 !== 0) {
                //画I点区域
                series.push(...getIArea([getFormatValue(count2), 0, 0, 0, 0, 1.8]));
                //画I点
                series.push(...getIPoints([0, 0, 0, 0, 0, 1.8]));
            }
            if(count4 === 0 && count3 !== 0) {
                //画I点区域
                series.push(...getIArea([0, 0, 0, 0, 1.8, getFormatValue(count3)]));
                //画I点
                series.push(...getIPoints([0, 0, 0, 0, 1.8, 0]));
            }
            //最后一个I点
            if(count4 !== 0) {
                series.push(...getIPoints([0, 0, 0, getFormatValue(count5), 0, 0]));
            }
            //覆盖圆点样式
            series.push({
                name: '',
                type: 'radar',
                symbol: 'circle',
                symbolSize: 6,
                itemStyle: {
                    normal: {
                        color: 'rgba(255,255,255,1)'
                    },
                },
                data: [{
                    value: [0]
                }],
            });
            return series;
        }
        function getIPoints(value: number[]) {
            return [{
                name: '',
                type: 'radar',
                symbol: 'circle',
                symbolSize: 5,
                lineStyle: {
                    width: 0
                },
                itemStyle: {
                    normal: {
                        color: 'rgba(0,0,0,0.1)'
                    },
                },
                data: [{
                    value: value
                }],
            },
            {
                name: '',
                type: 'radar',
                symbol: 'circle',
                symbolSize: 4,
                lineStyle: {
                    width: 0
                },
                itemStyle: {
                    normal: {
                        color: 'rgba(255,255,255,1)'
                    },
                },
                data: [{
                    value: value
                }],
            }]
        }
        function getIArea(value: number[]) {
            return [{
                name: '',
                type: 'radar',
                symbol: 'circle', // 拐点的样式，还可以取值'rect','angle'等
                symbolSize: 3, // 拐点的大小
                animation: false,
                itemStyle: {
                    normal: {
                        color: 'transparent'
                    },
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0,
                        y: 1,
                        x2: 0,
                        y2: 0,
                        colorStops: colorStops
                    }
                },
                z: 99,
                lineStyle: {
                    width: 0
                },
                data: [{
                    value: value
                }],
            }]
        }
    }, [props]);
    function getColorByData(x: number, type?: string) {
        const map = [
            { x: 1000.0, k_r: -0.044, b_r: 277.0, k_g: -0.23, b_g: 269.0, k_b: 0.078, b_b: -39.0 },
            { x: 500.0, k_r: -0.044, b_r: 277.0, k_g: -0.23, b_g: 269.0, k_b: 0.078, b_b: -39.0 },
            { x: 200.0, k_r: 0.373, b_r: 68.333, k_g: 0.263, b_g: 22.333, k_b: -0.793, b_b: 396.667 },
            { x: 50.0, k_r: 0.567, b_r: 29.667, k_g: -0.393, b_g: 153.667, k_b: -0.113, b_b: 260.667 },
            { x: 10.0, k_r: -0.65, b_r: 90.5, k_g: -2.5, b_g: 259.0, k_b: 1.025, b_b: 203.75 },
            { x: 1.0, k_r: -5.556, b_r: 139.556, k_g: 0.444, b_g: 229.556, k_b: 15.0, b_b: 64.0 },
            { x: 0.0, k_r: 242.0, b_r: 0.0, k_g: 243.0, b_g: 0.0, k_b: 245.0, b_b: 0.0 }
        ];

        let curP = map[map.length - 1];
        for (const p of map) {
            if (x >= p.x) {
                curP = p;
                break;
            }
        }

        const r = (curP.k_r * x + curP.b_r);
        const g = (curP.k_g * x + curP.b_g);
        const b = (curP.k_b * x + curP.b_b);
        return `rgba(${r}, ${g}, ${b}, ${type === 'opacity' ? 0.3 : 1})`;
    }
    function getCircleGradientData() {
        let countData: { offset: number, count: number, r: number }[] = [];
        const sortCountData = [count1, count2, count3, count4].sort((a, b) => a - b);
        //提前获取最高挡位数据
        const [sortMaxValue, sortIndex] = getMaxValue(sortCountData[3]);
        const allData = sortIndex + sortCountData[3]/sortMaxValue;
        sortCountData.map((item, index) => {
            const [value, idx] = getMaxValue(sortCountData[index]);
            //拿到每个数据折算成挡位的值
            const offset = idx + sortCountData[index]/value;
            //offset/allData 折算成最高数据的百分比
            countData.push({offset: offset ? offset/allData : 0, count: sortCountData[index], r: offset ? offset/(idx + 1) - 0.1 : 0})
        })
        const xOffsetLeftIndex = sortCountData.findIndex((item) => item === count1);
        const xOffsetRightIndex = sortCountData.findIndex((item) => item === count4);
        const xOffset = (xOffsetLeftIndex === 0 || xOffsetRightIndex === 0)
            ? 0.5
            : countData[xOffsetLeftIndex].offset/(countData[xOffsetLeftIndex].offset + countData[xOffsetRightIndex].offset);
        const xOffsetTopLeftIndex = sortCountData.findIndex((item) => item === count2);
        const xOffsetTopRightIndex = sortCountData.findIndex((item) => item === count3);
        const yOffset = Math.max(countData[xOffsetTopLeftIndex].offset, countData[xOffsetTopRightIndex].offset) - 0.2;
        const rOffset = Math.max(countData[xOffsetLeftIndex].r, countData[xOffsetRightIndex].r, countData[xOffsetTopLeftIndex].r, countData[xOffsetTopRightIndex].r);
        return {countData, xOffset, yOffset, rOffset}
    }

    const { statisticsData } = props;
    if(statisticsData.length === 0) return <></>
    const count1 = statisticsData[0] && statisticsData[0].count || 0;
    const count2 = statisticsData[1] && statisticsData[1].count || 0;
    const count3 = statisticsData[2] && statisticsData[2].count || 0;
    const count4 = statisticsData[3] && statisticsData[3].count || 0;
    const count5 = statisticsData[4] && statisticsData[4].count || 0;
    const count6 = statisticsData[5] && statisticsData[5].count || 0;
    const RangeMap = new Map([
        [UserTabType.Group, `linear-gradient(135deg, rgba(255, 255, 255, 1), ${getColorByData(count1)}`],
        [UserTabType.Create, `linear-gradient(227deg, rgba(255, 255, 255, 1), ${getColorByData(count2)}`],
        [UserTabType.Spread, `linear-gradient(225deg, rgba(255, 255, 255, 1), ${getColorByData(count3)})`],
        [UserTabType.Consume, `linear-gradient(135deg, rgba(255, 255, 255, 1), ${getColorByData(count4)})`],
        [UserTabType.i, `linear-gradient(135deg, ${getColorByData(count5)}, rgba(255, 255, 255, 1))`],
        ['5', `linear-gradient(135deg, ${getColorByData(count6)}, rgba(255, 255, 255, 1))`],
    ])

    return <div className={styles.container}>
        <div className={styles.mainWrap}>
            <ChartTag chartRef={chartRef} />
            {
                sixStatisticsData.map((item, index) => (
                    item === null || item.type === UserTabType.i ? <div key={index} className={classNames([styles[`line${index}`], styles[`noDataLine${index}`]])} style={{background: '#F6F6F6'}}></div> :
                        <div key={index} className={classNames([styles.itemWrap, styles[`itemWrap${item.type}`], (item.icon === 'icon-chuanboli1' && item.type === UserTabType.Create) && styles.itemUserWrap])} onClick={() => props.setActiveBtn && props.setActiveBtn(item.type)}>
                        <div className={classNames([styles[`number${index}`],styles.number])} style={{marginRight: item.icon === 'icon-chuanboli1' ? '0' : '4px'}}>{item.count}</div>
                        {
                            item.flex === 'column' ? <>
                                <div className={classNames([styles.iconWrap, styles.iconRowWrap])}>
                                    <div className={styles.iconBg} style={{background: getColorByData(item.count)}}></div>
                                    <div className={classNames([styles.icon, `iconfont ${item.icon}`, item.icon === 'icon-xiaofeirenshu' && styles.activeIcon, item.icon === 'icon-jiaoyipianshu' && styles.active2Icon])}
                                         style={{color: item.count == 0 ? '#969799' : getColorByData(item.count)}}></div>
                                </div>
                                <div className={styles.text}>{item.title}</div>
                            </> : <div className={classNames([styles.contentWrap, styles[`contentWrap${item.type}`], (item.icon === 'icon-chuanboli1' && item.type === UserTabType.Create) && styles.contentUserWrap])}>
                                <div className={styles.text} style={{width: item.type === GroupTabType.historyHighest ? '100px' : 'auto', marginRight: (sixStatisticsData[0]?.count === 0 && window.innerWidth < 1330) ? '10px' : '0'}}>{item.title}</div>
                                <div className={styles.iconWrap}>
                                    <div className={styles.iconBg} style={{background: getColorByData(item.count)}}></div>
                                    <div className={classNames([styles.icon, `iconfont ${item.icon}`, item.icon === 'icon-xiaofeirenshu' && styles.activeIcon, item.icon === 'icon-jiaoyipianshu' && styles.active2Icon])}
                                         style={{color: item.count == 0 ? '#969799' : getColorByData(item.count)}}></div>
                                </div>
                            </div>
                        }
                        <div className={styles[`line${item.type}`]} style={{background: item.count === 0 ? '#F6F6F6' : RangeMap.get(item.type), left: (item.icon === 'icon-chuanboli1' && item.type === UserTabType.Create && window.innerWidth < 1330) ? '138px'
                                :(item.icon === 'icon-chuanboli1' && item.type === UserTabType.Create && window.innerWidth > 1330) ? '146px' : ''}}>
                            {item.count > colorData[colorData.length - 1].threshold && <div className={styles.overPoints}></div>}
                        </div>
                    </div>
                ))
            }
        </div>
        <div className={styles.timeWrap}>
            <div className={styles.time}>{formatTime(props.dataUpdateTime)}</div>
            <div className={styles.timeLine}></div>
            <div className={styles.version}>ver.01</div>
        </div>
        <div className={styles.colorLineWrap}>
            <div className={styles.colorLine}></div>
        </div>
    </div>;
};
