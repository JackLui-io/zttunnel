package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 隧道参数模板-方向子表
 */
@TableName("tunnel_param_template_direction")
@Data
public class TunnelParamTemplateDirection implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private Integer sortOrder;
    /** 1右线 2左线 */
    private Integer direction;
    private String lineDisplayName;
    private BigDecimal lineTunnelMileage;
    private Integer lineStatus;
    private String payloadJson;
    private Date createTime;
    private Date updateTime;
}
