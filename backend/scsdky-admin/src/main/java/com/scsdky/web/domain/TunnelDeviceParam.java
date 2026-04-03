package com.scsdky.web.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * 边缘控制器参数信息(TunnelDeviceParam)表实体类
 *
 * @author makejava
 * @since 2025-08-26 09:26:45
 */
@Data
@TableName(value ="tunnel_device_param")
public class TunnelDeviceParam {
    //自增主键
    @TableId(type = IdType.AUTO)
    private Long id;
    //边缘控制器设备id
    private Long devicelistId;
    //蓝牙ID
    private String bluetoothId;
    //边缘状态上报间隔（s）
    private Integer edgeStatusReportInterval;
    //灯具状态上报间隔（s）
    private Integer lampStatusReportInterval;
    //灯具与边缘通信超时（s）
    private Integer lampEdgeCommTimeout;
    //灯具终端同步周期（s）（AT+LAMPCYC）
    private Integer lampTerminalSyncCycle;
    //设置灯具终端状态上报时间（s）（AT+LAMPSTAERPTCYC）
    private Integer lampTerminalReportTime;
    //灯具超时周期（AT+TESTFUN=2,1200）
    private Integer lampTimeoutCycle;
    //设置工况汇报周期（s）（AT+RPTCYC=?）
    private Integer workingConditionReportCycle;
    //灯具终端限制设置默认周期（AT+BASECFGCYC=10）
    private Integer lampTerminalDefaultCycle;
    //灯具列表
    private String lampList;
    //灯具参数下发
    private String lampParamDelivery;
    //蓝牙节点（原“灯具广播”）
    private String bluetoothNode;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
}

