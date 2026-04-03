package com.scsdky.web.domain.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 引道灯控制器DTO
 *
 * @author makejava
 * @since 2025-09-29 10:13:56
 */
@Data
public class TunnelApproachLampsTerminalDto extends Model<TunnelApproachLampsTerminalDto> {
    //主键ID
    private Long id;
    //隧道ID
    private Long tunnelId;
    //设备号
    private Integer deviceNo;
    //安装里程
    private String installationMileage;
    //区段
    private Integer zone;
    //区段名称
    private String zoneName;
    //状态（0=离线，1=在线）
    private Integer status;
    //最后一次刷新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdate;
    //蓝牙强度
    private Integer bluetoothStrength;
    //版本号
    private Integer version;
}

