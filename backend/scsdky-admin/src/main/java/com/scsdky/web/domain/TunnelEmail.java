package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件发送记录表
 * @TableName tunnel_check_email_history
 */
@Data
@TableName(value = "tunnel_check_email_history")
public class TunnelEmail implements Serializable {
    
    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    private static final long serialVersionUID = 1L;
}

