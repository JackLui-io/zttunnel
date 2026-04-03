package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author tubo
 * @TableName t_tunnel_name_result
 */
@TableName(value ="t_tunnel_name_result")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TunnelNameResult implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 隧道名称、线路名称
     */
    private String tunnelName;

    /**
     * 隧道里程
     */
    private BigDecimal tunnelMileage;

    /**
     * 父类id
     */
    private Long parentId;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0 有效  1失效
     */
    private Integer status;

    /**
     * l
     */
    private Integer level;

    @TableField(exist = false)
    private List<TunnelNameResult> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
