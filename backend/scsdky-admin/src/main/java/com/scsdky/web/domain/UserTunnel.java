package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 隧道-用户表
 * @TableName t_user_tunnel
 */
@TableName(value ="t_user_tunnel")
@Data
public class UserTunnel implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 隧道表id
     */
    private Long tunnelNameId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
