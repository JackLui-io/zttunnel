package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 边缘控制器设备上传参数
 * @TableName tunnel_edge_compute_data
 */
@TableName(value ="tunnel_edge_compute_data")
@Data
public class TunnelEdgeComputeData implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 边缘计算终端id
     */
    private Long devicelistId;

    /**
     * 按bit表示
bit0: 亮度仪工作状态 0:正常 1：异常
bit1: 雷达工作状态 0:正常1：异常
     */
    private Integer edgeComputeStatus0;

    /**
     * 按bit表示
bit0: 亮度仪工作状态 0:正常 1：异常
bit1: 雷达工作状态 0:正常1：异常
     */
    private Integer edgeComputeStatus1;

    /**
     * 按bit表示
bit0: 亮度仪工作状态 0:正常 1：异常
bit1: 雷达工作状态 0:正常1：异常
     */
    private Integer edgeComputeStatus2;

    /**
     * 按bit表示
bit0: 亮度仪工作状态 0:正常 1：异常
bit1: 雷达工作状态 0:正常1：异常
     */
    private Integer edgeComputeStatus3;

    /**
     * 洞外亮度仪提供 单位:cd/m2 小端
     */
    private Short luminance;

    /**
     * 车流量
     */
    private Short trafficFlow;

    /**
     * 调光比例
     */
    private Integer dimmingRatio;

    /**
     * 亮灯时间
     */
    private Long lightingTime;

    /**
     * 车速1
     */
    private Long speed1;

    /**
     * 车速2
     */
    private Long speed2;

    /**
     * 车速3
     */
    private Long speed3;

    /**
     * 车速4
     */
    private Long speed4;

    /**
     * 车速5
     */
    private Long speed5;

    /**
     * 车速6
     */
    private Long speed6;

    /**
     * 车速7
     */
    private Long speed7;

    /**
     * 车速8
     */
    private Long speed8;

    /**
     * 车速9
     */
    private Long speed9;

    /**
     * 车速10
     */
    private Long speed10;

    /**
     * 车速11
     */
    private Long speed11;

    /**
     * 车速12
     */
    private Long speed12;

    /**
     * 车速13
     */
    private Long speed13;

    /**
     * 车速14
     */
    private Long speed14;

    /**
     * 车速15
     */
    private Long speed15;

    /**
     * 车速16
     */
    private Long speed16;

    /**
     * 车速17
     */
    private Long speed17;

    /**
     * 车速18
     */
    private Long speed18;

    /**
     * 车速19
     */
    private Long speed19;

    /**
     * 车速20
     */
    private Long speed20;

    /**
     * 上传时间
     */
    private Date uploadTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
