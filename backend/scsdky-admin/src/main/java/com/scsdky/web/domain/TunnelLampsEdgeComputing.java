package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 灯具终端-边缘计算终端关系表
 * @TableName tunnel_lamps_edge_computing
 */
@TableName(value ="tunnel_lamps_edge_computing")
@Data
public class TunnelLampsEdgeComputing implements Serializable {
    /**
     * 灯具终端.唯一ID
     */
    @TableId(type = IdType.INPUT)
    private Long uniqueId;

    /**
     * 边缘计算终端设备号
     */
    private Long devicelistId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
