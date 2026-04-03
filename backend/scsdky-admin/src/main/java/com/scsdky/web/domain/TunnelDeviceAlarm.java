package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备巡检告警记录表
 * @TableName tunnel_check_alarm
 */
@Data
@TableName(value = "tunnel_check_alarm")
public class TunnelDeviceAlarm implements Serializable {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 类型ID（1=健康巡检报告，2=设备掉线通知，3=其他）
     */
    private Integer typeId;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 状态：1=按时上报（1.95倍expected_interval时间段内有上报信息），0=未按时上报（1.95倍expected_interval时间段内没有上报信息）
     */
    private Integer status;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    private static final long serialVersionUID = 1L;
}

