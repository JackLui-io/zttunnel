package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 隧道表
 * @TableName t_tunnel_name
 */
@TableName(value ="t_tunnel_name")
@Data
public class TunnelName implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 隧道名称
     */
    private String tunnelName;

    /**
     * 父类id
     */
    private Long parentId;

    /**
     * 0 有效  1失效
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 里程数（m）
     */
    private Double mileage;

    @TableField(exist = false)
    private List<TunnelName> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
