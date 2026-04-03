package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName tunnel_syscmd
 */
@TableName(value ="tunnel_syscmd")
@Data
public class TunnelSyscmd implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long uniqueid;

    /**
     * 用户ID
     */
    private Long userid;

    /**
     * 指令
     */
    private String cmd;

    /**
     * 指令对象
     */
    private String json;

    /**
     * 添加时间
     */
    private Date addtime;

    /**
     * 回复状态
     */
    private Integer rspstate;

    /**
     * 目标设备号
     */
    private Long target;

    /**
     * 回复时间
     */
    private Date rsptime;

    /**
     * 是否回复
     */
    private Integer isrsp;

    /**
     * 是否已读
     */
    private Integer readed;

    /**
     * 回复数据
     */
    private String rspdata;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
