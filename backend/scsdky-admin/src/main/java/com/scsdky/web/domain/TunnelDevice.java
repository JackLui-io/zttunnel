package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备表
 * @TableName t_tunnel_device
 */
@TableName(value ="t_tunnel_device")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TunnelDevice implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 隧道名称
     */
    @Excel(name = "隧道名称")
    @ApiModelProperty(value = "隧道名称")
    private String tunnelName;

    @ApiModelProperty(value = "信号强度")
    private Integer csq;

    @ApiModelProperty(value = "密码")
    private Long devicePassword;

    /**
     * 隧道编号
     */
    @ApiModelProperty(value = "隧道编号")
    private Long tunnelId;

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
     * 设备id
     */
    @Excel(name = "设备id")
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    /**
     * 设备类型
     */
    @Excel(name = "设备类型")
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 设备状态
     */
    @Excel(name = "设备状态")
    @ApiModelProperty(value = "设备状态")
    private String deviceStatus;

    @ApiModelProperty(value = "设备属性")
    private String deviceProperty;

    @ApiModelProperty(value = "雷达设备号")
    private String ldDeviceId;

    @ApiModelProperty(value = "雷达状态")
    private String ldStatus;

    @ApiModelProperty(value = "是否安装雷达 0 无 1有")
    private Integer ldWhetherInstall;


    /**
     * 设备桩号
     */
    @Excel(name = "设备桩号")
    @ApiModelProperty(value = "设备桩号")
    private String deviceNum;

    @Excel(name = "设备名称")
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateTime;

    @Excel(name = "灯具终端表id")
    @ApiModelProperty(value = "灯具终端表id")
    private Long uniqueId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
