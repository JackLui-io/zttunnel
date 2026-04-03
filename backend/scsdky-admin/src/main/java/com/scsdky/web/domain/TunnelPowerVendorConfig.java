package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 电能终端厂商配置
 */
@Data
@TableName("tunnel_power_vendor_config")
public class TunnelPowerVendorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "vendorid", type = IdType.INPUT)
    private Integer vendorId;

    @TableField("vendorname")
    private String vendorName;

    private String description;

    @TableField("created_time")
    private LocalDateTime createdTime;
}
