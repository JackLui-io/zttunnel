package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备期望间隔时间表
 * @TableName tunnel_check_expected_interval
 */
@Data
@TableName(value = "tunnel_check_expected_interval")
public class TunnelDeviceExpectedInterval implements Serializable {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 设备ID
     */
    private Long uniqueId;
    
    /**
     * 类型：0=来自tunnel_devicelist，1=来自tunnel_lamps_terminal
     */
    private Integer type;
    
    /**
     * 预期上报间隔（秒）
     * 注意：根据用户提供的表结构，此字段可能不存在，如果表结构中没有此字段，请删除此属性
     */
    private Integer expectedInterval;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    private static final long serialVersionUID = 1L;
}

