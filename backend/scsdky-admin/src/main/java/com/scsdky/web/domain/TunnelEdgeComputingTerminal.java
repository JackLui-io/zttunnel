package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 隧道参数基本表
 * @TableName tunnel_edge_computing_terminal
 */
@TableName(value ="tunnel_edge_computing_terminal")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TunnelEdgeComputingTerminal implements Serializable {
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
     * 隧道方向
     */
    private String direction;


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
     * 物理区段(入口段、过渡段、中间段、出口段)
     */
    private Integer zone;

    /**
     * 物理区段里程编号(四个区段的起点、中间点和终点里程编号)
     */
    private String zoneNum;

    /**
     * 入口里程编号
     */
    private Integer inMileageNum;

    /**
     * 出口里程编号
     */
    private Integer outMileageNum;

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
     * 灯具异常时长，灯具终端与边缘计算终端通信超时时长：秒
     */
    private Integer offlineTimeout;

    /**
     * 超亮系数(初始设计超过标准的系数
例：1.1)
     */
    private BigDecimal coeffL;

    /**
     * 中间段亮度(隧道中间段的亮度要求
例：1800cd/m2)
     */
    private Short lin;

    /**
     * 大车流限值(随车模式与无极调光模式分界的车流量。
例：60veh/(5min))
     */
    private Short largeTraffic;

    /**
     * 本隧道电表数量
     */
    private Integer vMin;

    /**
     * 来车信号前方亮灯最小距离
     */
    private Integer lightDistance;

    /**
     * 本隧道电表数量
     */
    private Integer delay;

    /**
     * 异常慢速目标速度上限值
     */
    private Short vMax;

    /**
     * 日间和夜间灯光的分隔点
     */
    private Short tNight1;

    /**
     * 夜间和深夜灯光的分隔点
     */
    private Short tNight2;

    /**
     * 深夜和日间灯光的分隔点
     */
    private Short tNight3;

    /**
     * 折减系数
     */
    private Integer kBrightnessreduction;

    /**
     * 区分白天夜晚的洞外亮度值例：800cd/m2
     */
    private Short l20Day;

    /**
     * 洞外亮度设计值(设计的洞外亮度取值
例：2800cd/m2)
     */
    private Short l20Design;

    /**
     * 日间预设亮度大值(亮度异常判断
例：2800cd/m2)
     */
    private Short l20DayPreMax;

    /**
     * 日间预设亮度小值(亮度异常判断
例：800cd/m2)
     */
    private Short l20DayPreMin;

    /**
     * 灯具可调光的电压范围小值
     */
    private Short vRange1;

    /**
     * 灯具可调光的电压范围最大值
     */
    private Short vRange2;

    /**
     * 过压限值
     */
    private Short umax;

    /**
     * 失压限值
     */
    private Short umin;

    /**
     * 与设计功率比较报警限值（%）
     */
    private Short pd;

    /**
     * 与之前正常功率比较报警限值（%）
     */
    private Short po;

    /**
     * 设计时参考的紧急停车带亮度,cd/m2
     */
    private Short lemDesign;

    /**
     * 设计时参考的夜间全洞亮度,cd/m2
     */
    private Short lnt1Design;

    /**
     * 设计时参考的深夜全洞亮度,cd/m2
     */
    private Short lnt2Design;

    /**
     * 设计时参考的路面亮度转换系数，Lx/cd*m2
     */
    private Short alphaRoadDesign;

    /**
     * 入口段1使用的灯具额定功率
     */
    private Short p0Th1RatedPower;

    /**
     * 入口段2使用的灯具额定功率
     */
    private Short p0Th2RatedPower;

    /**
     * 过渡段1使用的灯具额定功率
     */
    private Short p0Tr1RatedPower;

    /**
     * 过渡段2使用的灯具额定功率
     */
    private Short p0Tr2RatedPower;

    /**
     * 基本段使用的灯具额定功率
     */
    private Short p0MidRatedPower;

    /**
     * 出口段1使用的灯具额定功率
     */
    private Short p0Ex1RatedPower;

    /**
     * 出口段2使用的灯具额定功率
     */
    private Short p0Ex2RatedPower;

    /**
     * 紧急停车带灯具额定功率
     */
    private Short p0EmRatedPower;

    /**
     * 最小亮灯比值
     */
    private Short lightRadioMin;

    /**
     * 最大亮灯比值
     */
    private Short lightRadioMax;

    /**
     * 入口段1灯具照射面积
     */
    private Short s1Th1LightArea;

    /**
     * 入口段2灯具照射面积
     */
    private Short s1Th2LightArea;

    /**
     * 过渡段1灯具照射面积
     */
    private Short s2Tr1LightArea;

    /**
     * 过渡段2灯具照射面积
     */
    private Short s2Tr2LightArea;

    /**
     * 基本段灯具照射面积
     */
    private Short s3MidLightArea;

    /**
     * 出口段1灯具照射面积
     */
    private Short s4Ex1LightArea;

    /**
     * 出口段2灯具照射面积
     */
    private Short s4Ex2LightArea;

    /**
     * 紧急停车带灯具照射面积
     */
    private Short s5EmLightArea;

    /**
     * 入口段1灯具数量
     */
    private Short n1Th1Number;

    /**
     * 入口段2灯具数量
     */
    private Short n1Th2Number;

    /**
     * 过渡段1灯具数量
     */
    private Short n2Tr1Number;

    /**
     * 过渡段2灯具数量
     */
    private Short n2Tr2Number;

    /**
     * 基本段灯具数量
     */
    private Short n3MidNumber;

    /**
     * 出口段1灯具数量
     */
    private Short n4Ex1Number;

    /**
     * 出口段2灯具数量
     */
    private Short n4Ex2Number;

    /**
     * 紧急停车带灯具数量
     */
    private Short n5EmNumber;

    /**
     * 设计时参考的灯具维护系数,保留一位小数
     */
    private BigDecimal mDesign;

    /**
     * 入口段1灯具光通量
     */
    private Short fai1Th1Lm;

    /**
     * 入口段2灯具光通量
     */
    private Short fai1Th2Lm;

    /**
     * 过渡段1灯具光通量
     */
    private Short fai2Tr1Lm;

    /**
     * 过渡段2灯具光通量
     */
    private Short fai2Tr2Lm;

    /**
     * 基本段灯具光通量
     */
    private Short fai3MidLm;

    /**
     * 出口段1灯具光通量
     */
    private Short fai4Ex1Lm;

    /**
     * 出口段2灯具光通量
     */
    private Short fai4Ex2Lm;

    /**
     * 紧急停车带灯具光通量
     */
    private Short fai5EmLm;

    /**
     * 隧道空间利用系数，保留两位小数
     */
    private Short uSpaceRate;

    /**
     * 设计时参考隧道布灯方式取值
     */
    private Short xLayoutDesign;

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
     * 故障发生信号(启动故障弹窗以及故障信息更新)
     */
    private Integer fault;

    /**
     * 故障恢复信号(0-无更新
1-故障信息更新)
     */
    private Integer faultEliminate;

    /**
     * 设备重启信号(0-无指令
1-重启设备)
     */
    private Integer deviceRestart;

    /**
     * 异常停车信号(启动故障弹窗
例：0表示正常，1表示有异常停车)
     */
    private Integer carIsStop;

    /**
     * 超时提醒信号(启动了固定功率模式超过设置时间时未恢复，启动恢复提醒弹窗)
     */
    private Integer modeTimeout;

    /**
     * 洞外亮度(调光算法输入、统计展示
例：1800cd/m2)
     */
    private Short l20;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    @ApiModelProperty("模式 1 固定功率模式  2 智慧调光 3 无极调光模式")
    private Integer mode;

    /**
     * 开始时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("开始时间-针对固定功率和无极调光模式")
    private Date startTime;

    /**
     * 截止时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("截止时间-针对固定功率和无极调光模式")
    private Date endTime;

    /**
     * 入口段1功率值
     */
    @ApiModelProperty("入口段1功率值")
    private Short th1PowerValue;

    /**
     * 入口段2功率值
     */
    @ApiModelProperty("入口段2功率值")
    private Short th2PowerValue;

    /**
     * 过渡段1功率值
     */
    @ApiModelProperty("过渡段1功率值")
    private Short tr1PowerValue;

    /**
     * 过渡段2功率值
     */
    @ApiModelProperty("过渡段2功率值")
    private Short tr2PowerValue;

    /**
     * 基本段功率值
     */
    @ApiModelProperty("基本段功率值")
    private Short midPowerValue;

    /**
     * 出口段功率值
     */
    @ApiModelProperty("出口段功率值")
    private Short ex1PowerValue;

    /**
     * 紧急停车带功率值
     */
    @ApiModelProperty("紧急停车带功率值")
    private Short emPowerValue;

    /**
     * 调光控制器数量
     */
    @ApiModelProperty("调光控制器数量")
    private Short numlamp;

    /**
     * 紧急停车带控制器数
     */
    @ApiModelProperty("紧急停车带控制器数")
    @TableField(value = "numlamPemergent", fill = FieldFill.DEFAULT)
    private Short numlampPemergent;

    /**
     * 洞外来车开灯控制器数量
     */
    @ApiModelProperty("洞外来车开灯控制器数量")
    @TableField(value = "numlamPbasic", fill = FieldFill.DEFAULT)
    private Short numlampPbasic;

    /**
     * 洞外来车开灯控制器起始桩号
     */
    @ApiModelProperty("洞外来车开灯控制器起始桩号")
    private Integer basicStart;

    /**
     * 洞外来车开灯控制器结束桩号
     */
    @ApiModelProperty("洞外来车开灯控制器结束桩号")
    private Integer basicEnd;

    /**
     * 区段总数
     */
    @ApiModelProperty("区段总数")
    @TableField(value = "sectionNum", fill = FieldFill.DEFAULT)
    private Short sectionNum;

    /**
     * 设计速度
     */
    @ApiModelProperty("设计速度")
    private Short designV;

    /**
     * 车流上报重复次数
     */
    @ApiModelProperty("车流上报重复次数")
    private Integer numberOfRepeats;

    @ApiModelProperty(value = "入库车流量限值（5min）", example = "100")
    private Integer entryTrafficLimit;

    @ApiModelProperty(value = "入库车速限值（km/h）", example = "50")
    private Integer entrySpeedLimit;

    @ApiModelProperty(value = "备用1", example = "备用信息1")
    private String backup1;

    @ApiModelProperty(value = "备用2", example = "备用信息2")
    private String backup2;

    @ApiModelProperty(value = "备用3", example = "备用信息3")
    private String backup3;

    @ApiModelProperty(value = "备用4", example = "备用信息4")
    private String backup4;

    @ApiModelProperty(value = "备用5", example = "备用信息5")
    private String backup5;

    /**
     * 预亮灯控制器配置
     * 格式：40个值用英文半角逗号分隔的字符串
     * 示例：8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,200,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
     * 说明：
     *   - 共40个值，支持20行配置
     *   - 前20个值：等待时长（秒），范围0-255
     *   - 后20个值：持续时长（秒），范围0-255
     *   - 等待时长：检测到车辆后延迟多久开灯
     *   - 持续时长：灯具保持点亮的时间
     */
    @ApiModelProperty(value = "预亮灯控制器配置", example = "8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,200,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0")
    @TableField(exist = false)
    private String preOnConfig;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
