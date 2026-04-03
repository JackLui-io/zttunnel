package com.scsdky.web.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.scsdky.common.annotation.Excel;
import com.scsdky.web.domain.TunnelLampsTerminalNode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 灯具终端表
 * @TableName tunnel_lamps_terminal
 */
@Data
public class TunnelLampsTerminalDto implements Serializable {
    /**
     *  id
     */
    private Long uniqueId;

    /**
     * 设备号
     */
    @Excel(name = "设备号")
    private Long deviceId;

    /**
     * 设备属性
     */
    private String deviceProperty;

    @Excel(name = "灯具序号")
    private Integer position;

    /**
     * 区段1
     */
    private Integer zone;

    /**
     * 区段1名字
     */
    @Excel(name = "区段1")
    @TableField(exist = false)
    private String zoneName;

    /**
     * 区段2
     */
    private Integer zone2;

    /**
     * 区段2名字
     */
    @Excel(name = "区段2")
    @TableField(exist = false)
    private String zone2Name;

    /**
     * 回路编号
     */
    @Excel(name = "回路编号")
    private String loopNumber;

    /**
     * 设备状态
     */
    private String deviceStatus;

    @Excel(name = "雷达设备号")
    private String ldDeviceId;

    private String ldStatus;

    @Excel(name = "雷达状态")
    private String ldStatusName;

    @Excel(name = "是否安装雷达 0 未安装 1 已安装")
    private Integer ldWhetherInstall;

    /**
     * 调光类型 0 无级调光 1 随车调光
     */
    private Integer dimmingType;


    /**
     * 设备桩号
     */
    private Integer deviceNum;

    /**
     * 设备桩号
     */
    @Excel(name = "设备桩号")
    private String deviceNumStr;


    private String deviceName;

    private Long tunnelId;

    /**
     * 灯具状态
     */
    private Integer lampsStatus;

    /**
     * 通信状态 0 正常 1 异常
     */
    private Integer communicationState;

    /**
     * 通信状态 0 正常 1 异常
     */
    @Excel(name = "通信状态")
    private String communicationStateStr;

    /**
     * 工作状态 0 正常 1 异常
     */
    private Integer workState;

    /**
     * 工作状态 0 正常 1 异常
     */
    @Excel(name = "工作状态")
    private String workStateStr;

    /**
     * 信号轻度
     */
    @Excel(name = "信号轻度")
    private Integer csq;


    /**
     * 蓝牙编号
     */
    @Excel(name = "蓝牙编号")
    private String bluetoothNum;


    private String bluetoothValue;

    private String CH1;

    private String CH2;

    /**
     * 更新时间
     */
    @Excel(name = "更新时间")
    private Date updateTime;

    /**
     * 电力载波
     */
    @Excel(name = "电力载波")
    private String powerCarrier;

    /**
     * 节点信息数组
     */
    private List<TunnelLampsTerminalNode> nodeList;

    private static final long serialVersionUID = 1L;

}
