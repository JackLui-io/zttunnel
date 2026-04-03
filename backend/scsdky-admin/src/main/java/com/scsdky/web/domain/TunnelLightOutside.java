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
 * 洞内外照度
 * @TableName t_tunnel_light_outside
 */
@TableName(value ="t_tunnel_light_outside")
@Data
public class TunnelLightOutside implements Serializable {
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
     * 洞外亮度最大值
     */
    private Integer maxOutside;

    /**
     * 洞外亮度最小值
     */
    private Integer minOutside;
    private BigDecimal avgOutside;

    private Integer outsideLightValue;

    private Integer dimmingRadioValue;
    /**
     * 灯亮时长
     */
    private Integer lightUp;

    /**
     * 灯暗时长
     */
    private Integer lightDown;

    /**
     * 最大调光比例
     */
    private String maxDimmingRadio;

    /**
     * 最小调光比列
     */
    private String minDimmingRadio;
    private BigDecimal avgDimmingRadio;



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
