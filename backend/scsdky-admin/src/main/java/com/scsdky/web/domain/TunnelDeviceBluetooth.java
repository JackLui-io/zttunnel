package com.scsdky.web.domain;

import java.util.Date;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * 边缘控制器蓝牙节点信息(TunnelDeviceBluetooth)表实体类
 *
 * @author makejava
 * @since 2025-09-18 15:44:04
 */
@Data
public class TunnelDeviceBluetooth {
    //自增主键
    private Long id;
    //边缘控制器设备id
    private Long devicelistId;
    //蓝牙节点
    private String bluetoothNode;
    //是否直联（1表示是，0表示否）
    private Integer isDirectConnection;
    //最后一次刷新时间
    private Date lastUpdate;
}

