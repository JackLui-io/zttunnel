package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 直流照明-供电回路表(com.scsdky.web.test.TunnelPowerSupplyCircuit)表实体类
 *
 * @author makejava
 * @since 2025-08-25 13:54:05
 */
@Data
@TableName(value ="tunnel_power_supply_circuit")
public class TunnelPowerSupplyCircuit {
    //自增主键
    private Long id;
    //隧道ID
    private Long tunnelId;
    //回路
    private String loopId;
    //供电状态
    private String powerSupplyStatus;
    //对应区段
    private Integer correspondingSection;
    //状态字段
    private String statusField;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateTime;
}

