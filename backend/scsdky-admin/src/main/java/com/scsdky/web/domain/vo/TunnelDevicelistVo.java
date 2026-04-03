package com.scsdky.web.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 边缘计算终端基本响应对象
 * @TableName tunnel__devicelist
 */
@Data
public class TunnelDevicelistVo implements Serializable {
    /**
     * 设备号
     */
    @Excel(name = "设备号")
    private Long deviceId;

    /**
     * 设备密码
     */
    @Excel(name = "设备密码")
    private Long devicePassword;

    /**
     * 设备分类 1 边缘计算终端 2 电能表采集终端
     */
    private Integer deviceTypeId;

    /**
     * 信号强度(4G终端有效)
     */
    @Excel(name = "信号强度(4G终端有效)")
    private Integer csq;

    /**
     * 终端名称
     */
    @Excel(name = "终端名称")
    private String nickName;

    /**
     * 最后一次刷新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdate;


    /**
     * 区段
     */
    @ApiModelProperty(value = "区段")
    private String zone;

    /**
     * 回路编号
     */
    @ApiModelProperty(value = "回路编号")
    private String loopNumber;

    /**
     * 设备状态
     */
    @ApiModelProperty(value = "设备状态")
    private String deviceStatus;

    /**
     * 设备桩号
     */
    @ApiModelProperty(value = "设备桩号")
    private Integer deviceNum;

    /**
     * 设备在线状态
     */
    @ApiModelProperty(value = "设备在线状态 0  在线  1 离线")
    @Excel(name = "设备在线状态 0  在线  1 离线")
    private Integer online;

    /**
     * 当前工作模式
     */
    @ApiModelProperty(value = "当前工作模式")
    @Excel(name = "当前工作模式")
    private Integer workmode;

    /**
     * 固件版本
     */
    @Excel(name = "固件版本")
    private String softVer;

    /**
     * 网络信息
     */
    @Excel(name = "网络信息")
    private String networkInfo;

    /**
     * 雷达状态
     */
    @Excel(name = "雷达状态")
    private String radarStatus;

    /**
     * 亮度仪状态
     */
    @Excel(name = "亮度仪状态")
    private String brightnessMeterStatus;

    /**
     * 电表数量
     */
    @Excel(name = "电表数量")
    private Integer electricityMeterNum;

    /**
     * 版本号
     */
    private Long version;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
