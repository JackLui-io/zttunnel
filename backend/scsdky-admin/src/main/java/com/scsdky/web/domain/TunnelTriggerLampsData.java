package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 灯具触发时间
 * @TableName tunnel_trigger_lamps_data
 */
@TableName(value ="tunnel_trigger_lamps_data")
@Data
public class TunnelTriggerLampsData implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 灯具终端id
     */
    private Long uniqueId;

    /**
     *  终端状态 bit0:通信状态  0:正常 1：异常
     *  bit1:是否有车  0:无车  1：有车
     *  bit2:雷达状态  0:正常  1:异常
     */
    private Integer lampsStatus;

    /**
     * 信号强度
     */
    private Integer csq;

    /**
     * 终端状态-备用
     */
    private Integer lampsStatusBak;

    /**
     * 通道1输出电压
     */
    private Integer outVoltage1;

    /**
     * 通道2输出电压
     */
    private Integer outVoltage2;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 该灯具异常停车消息发送，0未发送，1 已发送,防止灯具终端没有及时上报，获取最新的一条信息，都是这条消息，就会导致重复下发消息
     */
    private Integer messageSend;

    /**
     * 该灯具通信状态消息发送，0未发送，1 已发送,  防止灯具终端没有及时上报，获取最新的一条信息，都是这条消息，就会导致重复下发消息
     */
    private Integer communicationSend;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
