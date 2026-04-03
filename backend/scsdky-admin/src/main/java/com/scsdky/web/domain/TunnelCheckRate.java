package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备在线率阈值配置表 
 * @TableName tunnel_check_health_config
 */
@Data
@TableName(value = "tunnel_check_health_config")
public class TunnelCheckRate implements Serializable {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 在线率阈值（例如：0.95 表示 95%）
     */
    private Double rate;
    
    // /**
    //  * 更新时间
    //  */
    // private Date updateTime;
    
    // /**
    //  * 备注说明
    //  */
    // private String remark;
    
    private static final long serialVersionUID = 1L;
}
