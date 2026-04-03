package com.scsdky.web.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 直流照明-供电回路新增对象
 *
 * @author makejava
 * @since 2025-08-25 13:54:05
 */
@Data
public class TunnelPowerSupplyCircuitDto {
    //自增主键
    @TableId(type = IdType.AUTO)
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
}

