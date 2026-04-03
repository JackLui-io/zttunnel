package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 车流车速每日数据
 * @TableName tunnel_inside_outside_day
 */
@TableName(value ="tunnel_inside_outside_day")
@Data
public class TunnelInsideOutsideDay {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 灯亮时长
     */
    private String lightUp;

    /**
     * 灯暗时长
     */
    private String lightDown;

    /**
     * 洞外亮度最大值
     */
    private Integer maxOutside;

    /**
     * 洞外亮度最小值
     */
    private Integer minOutside;

    /**
     * 洞外亮度平均值
     */
    private Integer avgOutside;

    /**
     * 洞外亮度值
     */
    private Integer outsideLightValue;

    /**
     * 调光比列最大值
     */
    private Long maxDimmingRadio;

    /**
     * 边缘控制器id
     */
    private Long devicelistId;

    /**
     * 隧道id
     */
    private Long tunnelId;

    /**
     * 调光比例最小值
     */
    private Long minDimmingRadio;

    /**
     * 调光比例平均值
     */
    private Long avgDimmingRadio;

    /**
     * 上传时间
     */
    private String uploadTime;
}