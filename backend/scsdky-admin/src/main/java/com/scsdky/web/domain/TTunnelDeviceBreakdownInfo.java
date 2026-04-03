package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 设备故障代码表
 * @TableName t_tunnel_device_breakdown_info
 */
@TableName(value ="t_tunnel_device_breakdown_info")
@Data
public class TTunnelDeviceBreakdownInfo implements Serializable {
    /**
     * 设备代码
     */
    private String deivceNum;

    /**
     * 故障代码
     */
    private String breakdownNum;

    /**
     * 对应故障信息
     */
    private String breakdownInfo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
