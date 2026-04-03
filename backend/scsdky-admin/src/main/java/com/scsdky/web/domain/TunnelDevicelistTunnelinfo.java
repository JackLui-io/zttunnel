package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 隧道与边缘计算终端的关系表
 * @TableName tunnel_devicelist_tunnelinfo
 */
@TableName(value ="tunnel_devicelist_tunnelinfo")
@Data
public class TunnelDevicelistTunnelinfo implements Serializable {
    /**
     * 隧道唯一id
     */
    private Long tunnelId;

    /**
     * 边缘计算终端表id
     */
    private Long devicelistId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * type 1 边缘 2 电能
     */
    private Integer type;
}
