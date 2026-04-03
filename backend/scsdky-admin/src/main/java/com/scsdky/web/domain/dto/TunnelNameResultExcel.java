package com.scsdky.web.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * easyexcel 参数表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TunnelNameResultExcel implements Serializable {
    /**
     * id
     */
    @ExcelProperty("线路名称")
    private String  lineName;

    @ExcelProperty("线路里程（km）")
    private String lineMileage;

    @ExcelProperty("隧道总里程（km单洞）")
    private String lineMileageTunnel;

    /**
     * 隧道名称、线路名称
     */
    @ExcelProperty("隧道名称")
    private String tunnelName;

    /**
     * 隧道方向
     */
    @ExcelProperty("隧道方向")
    private String direction;

    /**
     * 隧道里程
     */
    @ExcelProperty("隧道里程")
    private String tunnelMileage;


    /**
     * 物理区段(入口段、过渡段、中间段、出口段)
     */
    @ExcelProperty("物理区段")
    private Integer zone;

    /**
     * 物理区段里程编号(四个区段的起点、中间点和终点里程编号)
     */
    @ExcelProperty("物理区段里程编号")
    private String zoneNum;

    /**
     * 入口里程编号
     */
    @ExcelProperty("入口里程编号")
    private String inMileageNum;

    /**
     * 出口里程编号
     */
    @ExcelProperty("出口里程编号")
    private String outMileageNum;

    /**
     * 原照明系统回路设置(入口加强1、入口加强2、过渡加强1、过渡加强2、过渡加强3、基本1、基本2、应急1、应急2、出口加强1、出口加强2等)
     */
    @ExcelProperty("原照明系统回路设置")
    private String loopNumber;

    /**
     * 原照明系统回路设计功率(各回路的功率值
     命名方式为：设计功率-回路编号)
     */
    @ExcelProperty("原照明系统回路设计功率（kw）")
    private String designOperatingPowerR1;

    /**
     * 参与智慧照明的总回路设计功率(各回路功率值总和)
     */
    @ExcelProperty("参与智慧照明的总回路设计功率（kw）")
    private Integer designOperatingPowerTotal;

    /**
     * 碳排放因子
     */
    @ExcelProperty("碳排放因子(kwh/tCO2e)")
    private BigDecimal carbonEmissionFactor;

    /**
     * 等效种树常数
     */
    @ExcelProperty("等效种树常数")
    private BigDecimal equivalentTreeConstant;

    /**
     * 减碳煤当量常数
     */
    @ExcelProperty("减碳煤当量常数(kgce)")
    private Integer carbonReductionCoalEquivalentConstant;


    /**
     * 灯具异常时长，灯具终端与边缘计算终端通信超时时长：秒
     */
    @ExcelProperty("灯具异常时长")
    private Integer offlineTimeout;

    /**
     * 超亮系数(初始设计超过标准的系数
     例：1.1)
     */
    @ExcelProperty("超亮系数")
    private BigDecimal coeffL;

    /**
     * 中间段亮度(隧道中间段的亮度要求
     例：1800cd/m2)
     */
    @ExcelProperty("中间段亮度")
    private Short lin;

    /**
     * 大车流限值(随车模式与无极调光模式分界的车流量。
     例：60veh/(5min))
     */
    @ExcelProperty("大车流限值(vel/5min)")
    private Short largeTraffic;

    /**
     * 停车预警速度限值(启动异常停车算法的速度限值
     例：3km/h)
     */
    @ExcelProperty("停车预警速度限值(m/s)")
    //@ExcelProperty("本隧道电表数量")
    private Integer vMin;

    /**
     * 来车信号前方亮灯最小距离
     */
    @ExcelProperty("来车信号前方亮灯最小距离（m)")
    private Integer lightDistance;

    /**
     * 车辆通过后延时关灯的时间 例：1s
     */
    @ExcelProperty("单洞电表数量")
    private Integer delay;

    /**
     * 异常慢速目标速度上限值
     */
    @ExcelProperty("异常慢速目标速度上限值")
    private Short vMax;

    /**
     * 日间和夜间灯光的分隔点
     */
    @ExcelProperty("日间和夜间灯光的分隔点")
    private Short tNight1;

    /**
     * 夜间和深夜灯光的分隔点
     */
    @ExcelProperty("夜间和深夜灯光的分隔点")
    private Short tNight2;

    /**
     * 深夜和日间灯光的分隔点
     */
    @ExcelProperty("深夜和日间灯光的分隔点")
    private Short tNight3;

    /**
     * 折减系数
     */
    @ExcelProperty("折减系数")
    private Integer kBrightnessreduction;

    /**
     * 区分白天夜晚的洞外亮度值例：800cd/m2
     */
    @ExcelProperty("通过亮度判断天黑、天亮的分界线")
    private Short l20Day;

    /**
     * 洞外亮度设计值(设计的洞外亮度取值
     例：2800cd/m2)
     */
    @ExcelProperty("设计时参考的洞外亮度参数")
    private Short l20Design;

    /**
     * 日间预设亮度大值(亮度异常判断
     例：2800cd/m2)
     */
    @ExcelProperty("日间预设亮度大值")
    private Short l20DayPreMax;

    /**
     * 日间预设亮度小值(亮度异常判断
     例：800cd/m2)
     */
    @ExcelProperty("日间预设亮度小值")
    private Short l20DayPreMin;

    /**
     * 灯具可调光的电压范围小值
     */
    @ExcelProperty("灯具可调光的电压范围小值")
    private Short vRange1;

    /**
     * 灯具可调光的电压范围最大值
     */
    @ExcelProperty("灯具可调光的电压范围大值")
    private Short vRange2;

    /**
     * 过压限值
     */
    @ExcelProperty("过压限值（V）")
    private Short umax;

    /**
     * 失压限值
     */
    @ExcelProperty("失压限值（V)")
    private Short umin;

    /**
     * 与设计功率比较报警限值（%）
     */
    @ExcelProperty("与设计功率比较报警限值（%）")
    private Short pd;

    /**
     * 与之前正常功率比较报警限值（%）
     */
    @ExcelProperty("与之前正常功率比较报警限值（%）")
    private Short po;

    /**
     * 设计时参考的紧急停车带亮度,cd/m2
     */
    @ExcelProperty("设计时参考的紧急停车带亮度,cd/m2")
    private Short lemDesign;

    /**
     * 设计时参考的夜间全洞亮度,cd/m2
     */
    @ExcelProperty("设计时参考的夜间全洞亮度,cd/m2")
    private Short lnt1Design;

    /**
     * 设计时参考的深夜全洞亮度,cd/m2
     */
    @ExcelProperty("设计时参考的深夜全洞亮度,cd/m2")
    private Short lnt2Design;

    /**
     * 设计时参考的路面亮度转换系数，Lx/cd*m2
     */
    @ExcelProperty("设计时参考的路面亮度转换系数，Lx/cd*m2")
    private Short alphaRoadDesign;

    /**
     * 入口段1使用的灯具额定功率
     */
    @ExcelProperty("入口段1使用的灯具额定功率")
    private Short p0Th1RatedPower;

    /**
     * 入口段2使用的灯具额定功率
     */
    @ExcelProperty("入口段2使用的灯具额定功率")
    private Short p0Th2RatedPower;

    /**
     * 过渡段1使用的灯具额定功率
     */
    @ExcelProperty("过渡段1使用的灯具额定功率")
    private Short p0Tr1RatedPower;

    /**
     * 过渡段2使用的灯具额定功率
     */
    @ExcelProperty("过渡段2使用的灯具额定功率")
    private Short p0Tr2RatedPower;

    /**
     * 基本段使用的灯具额定功率
     */
    @ExcelProperty("基本段使用的灯具额定功率")
    private Short p0MidRatedPower;

    /**
     * 出口段1使用的灯具额定功率
     */
    @ExcelProperty("出口段1使用的灯具额定功率")
    private Short p0Ex1RatedPower;

    /**
     * 出口段2使用的灯具额定功率
     */
    @ExcelProperty("出口段2使用的灯具额定功率")
    private Short p0Ex2RatedPower;

    /**
     * 紧急停车带灯具额定功率
     */
    @ExcelProperty("紧急停车带灯具额定功率")
    private Short p0EmRatedPower;

    /**
     * 最小亮灯比值
     */
    @ExcelProperty("最小亮灯比值")
    private Short lightRadioMin;

    /**
     * 最大亮灯比值
     */
    @ExcelProperty("最大亮灯比值")
    private Short lightRadioMax;

    /**
     * 入口段1灯具照射面积
     */
    @ExcelProperty("入口段1灯具照射面积")
    private Short s1Th1LightArea;

    /**
     * 入口段2灯具照射面积
     */
    @ExcelProperty("入口段2灯具照射面积")
    private Short s1Th2LightArea;

    /**
     * 过渡段1灯具照射面积
     */
    @ExcelProperty("过渡段1灯具照射面积")
    private Short s2Tr1LightArea;

    /**
     * 过渡段2灯具照射面积
     */
    @ExcelProperty("过渡段2灯具照射面积")
    private Short s2Tr2LightArea;

    /**
     * 基本段灯具照射面积
     */
    @ExcelProperty("基本段灯具照射面积")
    private Short s3MidLightArea;

    /**
     * 出口段1灯具照射面积
     */
    @ExcelProperty("出口段1灯具照射面积")
    private Short s4Ex1LightArea;

    /**
     * 出口段2灯具照射面积
     */
    @ExcelProperty("出口段2灯具照射面积")
    private Short s4Ex2LightArea;

    /**
     * 紧急停车带灯具照射面积
     */
    @ExcelProperty("紧急停车带灯具照射面积")
    private Short s5EmLightArea;

    /**
     * 入口段1灯具数量
     */
    @ExcelProperty("入口段1灯具数量")
    private Short n1Th1Number;

    /**
     * 入口段2灯具数量
     */
    @ExcelProperty("入口段2灯具数量")
    private Short n1Th2Number;

    /**
     * 过渡段1灯具数量
     */
    @ExcelProperty("过渡段1灯具数量")
    private Short n2Tr1Number;

    /**
     * 过渡段2灯具数量
     */
    @ExcelProperty("过渡段2灯具数量")
    private Short n2Tr2Number;

    /**
     * 基本段灯具数量
     */
    @ExcelProperty("基本段灯具数量")
    private Short n3MidNumber;

    /**
     * 出口段1灯具数量
     */
    @ExcelProperty("出口段1灯具数量")
    private Short n4Ex1Number;

    /**
     * 出口段2灯具数量
     */
    @ExcelProperty("出口段2灯具数量")
    private Short n4Ex2Number;

    /**
     * 紧急停车带灯具数量
     */
    @ExcelProperty("紧急停车带灯具数量")
    private Short n5EmNumber;

    /**
     * 设计时参考的灯具维护系数,保留一位小数
     */
    @ExcelProperty("设计时参考的灯具维护系数")
    private BigDecimal mDesign;

    /**
     * 入口段1灯具光通量
     */
    @ExcelProperty("入口段1灯具光通量")
    private Short fai1Th1Lm;

    /**
     * 入口段2灯具光通量
     */
    @ExcelProperty("入口段2灯具光通量")
    private Short fai1Th2Lm;

    /**
     * 过渡段1灯具光通量
     */
    @ExcelProperty("过渡段1灯具光通量")
    private Short fai2Tr1Lm;

    /**
     * 过渡段2灯具光通量
     */
    @ExcelProperty("过渡段2灯具光通量")
    private Short fai2Tr2Lm;

    /**
     * 基本段灯具光通量
     */
    @ExcelProperty("基本段灯具光通量")
    private Short fai3MidLm;

    /**
     * 出口段1灯具光通量
     */
    @ExcelProperty("出口段1灯具光通量")
    private Short fai4Ex1Lm;

    /**
     * 出口段2灯具光通量
     */
    @ExcelProperty("出口段2灯具光通量")
    private Short fai4Ex2Lm;

    /**
     * 紧急停车带灯具光通量
     */
    @ExcelProperty("紧急停车带灯具光通量")
    private Short fai5EmLm;

    /**
     * 隧道空间利用系数，保留两位小数
     */
    @ExcelProperty("隧道空间利用系数")
    private Short uSpaceRate;

    /**
     * 设计时参考隧道布灯方式取值
     */
    @ExcelProperty("设计时参考隧道布灯方式取值")
    private Short xLayoutDesign;

    @ApiModelProperty("模式 1 固定功率模式  2 智慧调光 3 无极调光模式")
    @ExcelProperty("模式设置")
    private Integer mode;

    /**
     * 开始时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("开始时间-针对固定功率和无极调光模式")
    @ExcelProperty("开始时间")
    private Date startTime;

    /**
     * 截止时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("截止时间-针对固定功率和无极调光模式")
    @ExcelProperty("截止时间")
    private Date endTime;

    /**
     * 入口段1功率值
     */
    @ApiModelProperty("入口段1功率值")
    @ExcelProperty("入口段1功率值")
    private Short th1PowerValue;

    /**
     * 入口段2功率值
     */
    @ApiModelProperty("入口段2功率值")
    @ExcelProperty("入口段2功率值")
    private Short th2PowerValue;

    /**
     * 过渡段1功率值
     */
    @ApiModelProperty("过渡段1功率值")
    @ExcelProperty("过渡段1功率值")
    private Short tr1PowerValue;

    /**
     * 过渡段2功率值
     */
    @ApiModelProperty("过渡段2功率值")
    @ExcelProperty("过渡段2功率值")
    private Short tr2PowerValue;

    /**
     * 基本段功率值
     */
    @ApiModelProperty("基本段功率值")
    @ExcelProperty("基本段功率值")
    private Short midPowerValue;

    /**
     * 出口段功率值
     */
    @ApiModelProperty("出口段功率值")
    @ExcelProperty("出口段功率值")
    private Short ex1PowerValue;

    /**
     * 紧急停车带功率值
     */
    @ApiModelProperty("紧急停车带功率值")
    @ExcelProperty("紧急停车带功率值")
    private Short emPowerValue;


    /**
     * 调光控制器数量
     */
    @ApiModelProperty("调光控制器数量")
    @ExcelProperty("调光控制器数量")
    private Short numlamp;

    /**
     * 紧急停车带控制器数
     */
    @ApiModelProperty("紧急停车带控制器数")
    @ExcelProperty("紧急停车带控制器数")
    private Short numlampPemergent;

    /**
     * 洞外来车开灯控制器数量
     */
    @ApiModelProperty("洞外来车开灯控制器数量")
    @ExcelProperty("洞外来车开灯控制器数量")
    private Short numlampPbasic;

    /**
     * 洞外来车开灯控制器起始桩号
     */
    @ApiModelProperty("洞外来车开灯控制器起始桩号")
    @ExcelProperty("洞外来车开灯控制器起始桩号")
    private String basicStart;

    /**
     * 洞外来车开灯控制器结束桩号
     */
    @ApiModelProperty("洞外来车开灯控制器结束桩号")
    @ExcelProperty("洞外来车开灯控制器结束桩号")
    private String basicEnd;

    /**
     * 区段总数
     */
    @ApiModelProperty("区段总数")
    @ExcelProperty("区段总数")
    private Short sectionNum;

    /**
     * 设计速度
     */
    @ApiModelProperty("设计速度")
    @ExcelProperty("设计速度")
    private Short designV;

    /**
     * 车流上报重复次数
     */
    @ApiModelProperty("车流上报重复次数")
    @ExcelProperty("车流上报重复次数")
    private Integer numberOfRepeats;


}
