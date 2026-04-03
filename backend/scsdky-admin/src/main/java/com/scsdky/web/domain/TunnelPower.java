package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 电能监测数据实时数据表
 * @TableName tunnel_power
 */
@TableName(value ="tunnel_power")
@Data
public class TunnelPower implements Serializable {
    /**
     * 唯一id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 电表设备id
     */
    private Long deviceId;

    /**
     * 实时电压
     */
    private Double voltage;

    /**
     * 实时电流
     */
    private Double current;

    /**
     * 报警电压低

     */
    private String alarmVoltagelow;

    /**
     * 报警电压高

     */
    private String alarmVoltageHigh;

    /**
     * 边缘控制器设备id
     */
    private Long devicelistId;

    /**
     * 区段
     */
    @Excel(name = "区段")
    @ApiModelProperty(value = "区段")
    private String zone;

    /**
     * 回路编号
     */
    @Excel(name = "回路编号")
    @ApiModelProperty(value = "回路编号")
    private String loopNumber;

    /**
     * 设备状态
     */
    @Excel(name = "设备状态")
    @ApiModelProperty(value = "设备状态")
    private String deviceStatus;

    /**
     * 设备桩号
     */
    @Excel(name = "设备桩号")
    @ApiModelProperty(value = "设备桩号")
    private String deviceNum;

    /**
     * 设备名称
     */
    @Excel(name = "设备名称")
    @ApiModelProperty(value = "设备名称")
    private String deviceName;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
