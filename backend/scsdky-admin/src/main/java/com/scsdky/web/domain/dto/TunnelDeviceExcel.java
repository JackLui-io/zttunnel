package com.scsdky.web.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备参数
 * @author tubo
 * @TableName t_tunnel_device
 */
@Data
public class TunnelDeviceExcel implements Serializable {

    /**
     * 隧道名称
     */
    @ExcelProperty("隧道名称")
    private String tunnelName;

    /**
     * 隧道名称
     */
    @ExcelProperty("隧道方向")
    private String tunnelDirection;


    /**
     * 回路编号
     */
    @ExcelProperty("所属回路")
    private String loopNumber;

    /**
     * 所属物理区段
     */
    @ExcelProperty("所属物理区段")
    private Integer zone;

    /**
     * 设备id
     */
    @ExcelProperty("设备号")
    private String deviceId;

    /**
     * 设备类型
     */
    @ExcelProperty("设备类型")
    private String deviceType;

    /**
     * 设备类型
     */
    @ExcelProperty("设备名称")
    private String deviceName;

    /**
     * 设备状态
     */
    @ExcelProperty("设备状态")
    private String deviceStatus;

    @ExcelProperty("属性")
    private String deviceProperty;

    @ExcelProperty("雷达设备号")
    private String ldDviceId;

    @ExcelProperty("雷达状态")
    private String ldStatus;

    @ExcelProperty("是否安装雷达")
    private Integer ldWhetherInstall;

    /**
     * 设备桩号
     */
    @ExcelProperty("安装里程")
    private String deviceNum;

    @ExcelProperty("灯具序号")
    private Integer position;
}
