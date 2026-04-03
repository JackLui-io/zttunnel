package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 电表数据
 * @TableName tunnel_power_data
 */
@TableName(value ="tunnel_power_data")
@Data
public class TunnelPowerData implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 电能终端id
     */
    private Long devicelistId;

    /**
     * 电表类别 电表1  电表2
     */
    private Long uniqueId;

    /**
     * 上传时间
     */
    private Date uploadTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
