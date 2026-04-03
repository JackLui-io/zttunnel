package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 车流、车速
 * @TableName t_tunnel_traffic_flow
 */
@TableName(value ="t_tunnel_traffic_flow")
@Data
public class TunnelTrafficFlow implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 隧道id
     */
    private Long tunnelId;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 车流量
     */
    private Integer trafficFlow;

    private Integer maxTrafficFlow;

    private Integer minTrafficFlow;

    /**
     * 最大车速
     */
    private Integer maxSpeed;

    /**
     * 最小车速
     */
    private Integer minSpeed;

    /**
     * 平均车速
     */
    private Double avgSpeed;

    /**
     * 具体车速
     */
    private Integer speed;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
