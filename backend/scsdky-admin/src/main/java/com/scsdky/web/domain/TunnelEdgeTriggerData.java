package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 边缘控制器异常状态提示
 * @TableName tunnel_edge_trigger_data
 */
@TableName(value ="tunnel_edge_trigger_data")
@Data
public class TunnelEdgeTriggerData implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 边缘控制器id
     */
    private Long devicelistId;

    /**
     * 模组状态
     */
    private Integer hardwareStatus;

    /**
     * 模组状态备用
     */
    private Integer loraStatusBak;

    /**
     * 是否下发 0 否 1是
     */
    private Integer messageSend;

    /**
     * 上传时间
     */
    private Date uploadTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
