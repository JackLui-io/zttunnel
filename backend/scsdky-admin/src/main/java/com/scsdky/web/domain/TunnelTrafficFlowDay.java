package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 车流车速每日数据
 * @TableName tunnel_traffic_flow_day
 */
@TableName(value ="tunnel_traffic_flow_day")
@Data
public class TunnelTrafficFlowDay {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
    private Integer avgSpeed;

    /**
     * 每日车流量
     */
    private Integer trafficFlow;

    /**
     * 最大车流
     */
    private Integer maxTrafficFlow;

    /**
     * 最小车流
     */
    private Integer minTrafficFlow;

    /**
     * 平均车流
     */
    private Integer avgTrafficFlow;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 边缘控制器id
     */
    private Long devicelistId;

    /**
     * 隧道id
     */
    private Long tunnelId;
}