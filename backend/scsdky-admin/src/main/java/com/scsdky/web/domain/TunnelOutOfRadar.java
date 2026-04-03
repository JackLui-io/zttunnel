package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.scsdky.common.annotation.Excel;
import lombok.Data;

/**
 * 洞外雷达
 * @TableName tunnel_out_of_radar
 */
@TableName(value ="tunnel_out_of_radar")
@Data
public class TunnelOutOfRadar implements Serializable {
    /**
     * 唯一id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 雷达设备id
     */
    @Excel(name = "雷达设备号")
    private Long deviceId;

    /**
     * 边缘控制器设备id
     */
    private Long devicelistId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备状态
     */
    @Excel(name = "设备状态")
    private String deviceStatus;

    /**
     * 设备桩号
     */
    @Excel(name = "设备桩号")
    private Integer deviceNum;

    /**
     * 回路编号
     */
    @Excel(name = "回路编号")
    private String loopNumber;

    /**
     * 区段
     */
    @Excel(name = "区段")
    private Integer zone;

    /**
     * 类型 1 洞外雷达 2 洞外亮度传感器
     */
    private Integer type;

    @TableField(exist = false)
    private Long tunnelId;
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
