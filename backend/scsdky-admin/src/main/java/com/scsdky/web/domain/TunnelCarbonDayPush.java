package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 隧道能碳每日数据
 * @TableName tunnel_carbon_day_push
 */
@TableName(value ="tunnel_carbon_day_push")
@Data
public class TunnelCarbonDayPush {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 电表读数
     */
    private BigDecimal powerValue;

    /**
     * 电能终端号
     */
    private Long devicelistId;

    /**
     * 地址号
     */
    private Integer addr;

    /**
     * 年
     */
    private String year;

    /**
     * 月
     */
    private String month;

    /**
     * 日
     */
    private String day;

    /**
     * 上传时间
     */
    private String uploadTime;
}