package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 统计分析
 * @author tubo
 * @TableName t_tunnel_statistics
 */
@TableName(value ="t_tunnel_statistics")
@Data
public class TunnelStatistics implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 上传时间
     */
    private Date uploadTime;
    /**
     * 原设计耗电量
     */
    private Integer originalPowerConsumption;

    /**
     * 原单位里程耗电量
     */
    private Integer originalUnitPowerConsumption;

    /**
     * 原设计运行功率
     */
    private Integer originalOperatingPower;

    /**
     * 原设计亮灯时间
     */
    private Integer originalLightUpTime;

    /**
     * 原设计碳排放量
     */
    private String originalCarbonEmission;

    /**
     * 实际耗电量
     */
    private Integer actualPowerConsumption;

    /**
     * 实际单位里程耗电量
     */
    private Integer actualUnitPowerConsumption;

    /**
     * 实际运行功率
     */
    private Integer actualOperatingPower;

    /**
     * 实际亮灯时间
     */
    private Integer actualLightUpTime;

    /**
     * 实际碳排放量
     */
    private String actualcarbonemission;

    /**
     * 隧道id
     */
    private Long tunnelId;

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
