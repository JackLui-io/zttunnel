package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 边缘计算终端基本表
 * @TableName tunnel_edge_computing_terminal
 */
@TableName(value ="tunnel_edge_computing_terminal")
@Data
public class TunnelEdgeComputingTerminal_bak implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.INPUT)
    private Long id;


    /**
     * 线路名称 如：玉楚高速
     */
    private String lineName;

    /**
     * 线路名称 如：玉楚高速
     */
    private String direction;

    /**
     * 线路编号(区分线路)
     */
    private Long lineId;

    /**
     * 线路里程 如:205km
     */
    private BigDecimal lineMileage;

    /**
     * 隧道总里程 如:50km
     */
    private BigDecimal lineMileageTunnel;

    /**
     * 夜间-白天分割时间点(早时间点，按线路管理
例：0-24的数字)
     */
    private Integer tNightDay;

    /**
     * 白天-夜间分割时间点(晚两个时间点，按线路管理
例：0-24的数字)
     */
    private Integer tDayNight;

    /**
     * 隧道名称
     */
    private String tunnelName;

    /**
     * 隧道id
     */
    private Long tunnelId;

    /**
     * 隧道里程
     */
    private BigDecimal tunnelMileage;

    /**
     * 入口里程编号
     */
    private String inMileageNum;


    /**
     * 出口里程编号
     */
    private String outMileageNum;
    /**
     * 物理区段(入口段、过渡段、中间段、出口段)
     */
    private String zone;

    /**
     * 物理区段里程编号(四个区段的起点、中间点和终点里程编号)
     */
    private String zoneNum;

    /**
     * 原照明系统回路设置(入口加强1、入口加强2、过渡加强1、过渡加强2、过渡加强3、基本1、基本2、应急1、应急2、出口加强1、出口加强2等)
     */
    private String loopNumber;

    /**
     * 原照明系统回路设计功率(各回路的功率值
命名方式为：设计功率-回路编号)
     */
    private String designOperatingPowerR1;

    /**
     * 参与智慧照明的总回路设计功率(各回路功率值总和)
     */
    private Integer designOperatingPowerTotal;

    /**
     * 设计亮度折减系数如：0.9
     */
    private BigDecimal kBrightnessReduction;

    /**
     * 超亮系数(初始设计超过标准的系数
例：1.1)
     */
    private BigDecimal coeffL;

    /**
     * 洞外亮度设计值(设计的洞外亮度取值
例：2800cd/m2)
     */
    private Integer l20Design;

    /**
     * 日间预设亮度大值(亮度异常判断
例：2800cd/m2)
     */
    private Integer l20DayPreMax;

    /**
     * 日间预设亮度小值(亮度异常判断
例：800cd/m2)
     */
    private Integer l20DayPreMin;

    /**
     * 可调电压值(0-10/5V的调光电压)
     */
    private String vRange;

    /**
     * 区分白天夜晚的洞外亮度值例：800cd/m2
     */
    private Integer l20Day;

    /**
     * 中间段亮度(隧道中间段的亮度要求
例：1800cd/m2)
     */
    private Integer lin;

    /**
     * 碳排放因子(计算碳排放量，暂时设置为0.573)
     */
    private BigDecimal carbonEmissionFactor;

    /**
     * 等效种树常数
     */
    private BigDecimal equivalentTreeConstant;

    /**
     * 减碳煤当量常数(计算减碳煤当量，暂时设置为29270)
     */
    private Integer carbonReductionCoalEquivalentConstant;

    /**
     * 大车流限值(随车模式与无极调光模式分界的车流量。
例：60veh/(5min))
     */
    private Integer largeTraffic;

    /**
     * 停车预警速度限值(启动异常停车算法的速度限值
例：3km/h)
     */
    private Integer vMin;

    /**
     * 来车信号前方亮灯最小距离
     */
    private Integer lightDistance;

    /**
     * 车辆通过后延时关灯的时间 例：1s
     */
    private Integer delay;

//    /**
//     * 故障发生信号(启动故障弹窗以及故障信息更新)
//     */
//    private Integer fault;
//
//    /**
//     * 故障恢复信号(0-无更新
//1-故障信息更新)
//     */
//    private Byte faultEliminate;
//
//    /**
//     * 设备重启信号(0-无指令
//1-重启设备)
//     */
//    private Byte deviceRestart;
//
//    /**
//     * 异常停车信号(启动故障弹窗
//例：0表示正常，1表示有异常停车)
//     */
//    private Byte carIsStop;
//
//    /**
//     * 超时提醒信号(启动了固定功率模式超过设置时间时未恢复，启动恢复提醒弹窗)
//     */
//    private Byte modeTimeout;
//
//    /**
//     * 洞外亮度(调光算法输入、统计展示
//例：1800cd/m2)
//     */
//    private Integer l20;
//
//    /**
//     * 分钟车流量(调光算法输入、统计展示
//例：60veh/min)
//     */
//    private Integer instantaneousTraffic;
//
//    /**
//     * 调光比例(调光指令输出、功率计算、亮暗灯时间计算
//例：0.8)
//     */
//    private Double dimingRatio;
//
//    /**
//     * 实时（行车）车速(调光算法输入、统计展示
//例：80km/h)
//     */
//    private Integer vehicleSpeed;
//
//    /**
//     * 耗电量(统计展示、总耗电量计算、节电量计算、节电率计算
//例：100kw)
//     */
//    private Integer powerConsumption;
//
//    /**
//     * 各回路电表读数(各回路耗电量统计、总耗电量计算、节电量计算、节电率计算)
//     */
//    private Integer circuitConsumption;
//
//    /**
//     * 调光模式切换指令(1-随车调光
//2-无级调光
//3-固定功率
//4-预设模式)
//     */
//    private Byte dimingMode;
//
//    /**
//     * 导出指令(0-无指令
//1-导出指令)
//     */
//    private Byte export;
//
//    /**
//     * 设备设置指令(0-无指令
//1-设置指令)
//     */
//    private Byte devicesSettup;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
