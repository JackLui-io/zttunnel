package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 能碳数据
 * @TableName t_tunnel_energy_carbon
 */
@TableName(value ="t_tunnel_energy_carbon")
@Data
public class TunnelEnergyCarbon implements Serializable {
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
     * 时间
     */
    private Date uploadTime;

    /**
     * 当日耗电量
     */
    private Integer dailyPowerConsumption;

    /**
     * 累计耗电量
     */
    private Integer cumulativePowerConsumption;

    /**
     * 累计总节电量
     */
    private Integer totalPowerSavings;

    /**
     * 节电量
     */
    private Integer dailyPowerSavingRate;

    /**
     * 碳减排量/当量媒
     */
    private String ratio;

    /**
     * 等效种树量
     */
    private String plantTrees;

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
