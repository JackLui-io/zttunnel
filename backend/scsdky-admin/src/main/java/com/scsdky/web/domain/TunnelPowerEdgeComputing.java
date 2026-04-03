package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @TableName tunnel_power_edge_computing
 */
@TableName(value ="tunnel_power_edge_computing")
@Data
public class TunnelPowerEdgeComputing implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long uniqueId;

    /**
     * 地址号
     */
    private Integer address;

    /**
     * 电能终端id
     */
    private Long devicelistId;

    /**
     * 序号
     */
    private Integer meterIndex;

    /**
     * 回路名称
     */
    private String loopName;

    /**
     * 方向 1 右线 2 左线
     */
    private Integer direction;

    /**
     * 厂商配置ID
     */
    private Integer vendorId;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 是否为虚拟表
     */
    private Integer isVirtual;

    @TableField(exist = false)
    private String lastTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
