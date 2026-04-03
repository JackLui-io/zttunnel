package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通知公告
 * @TableName t_tunnel_notice
 */
@TableName(value ="t_tunnel_notice")
@Data
public class TunnelNotice implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 通知内容
     */
    @ApiModelProperty(value = "通知内容")
    private String content;

    /**
     * 类型 1 突发 2 线路 3 调光 4 设备
     */
    @ApiModelProperty(value = "类型 1 系统通知 2 操作提醒 3 实时警报 4 设备")
    private Integer type;

    /**
     * 类型 隧道id
     */
    @ApiModelProperty(value = "隧道id 路线级统计")
    private Long tunnelId;

    /**
     * 类型名称
     */
    @ApiModelProperty(value = "类型名称")
    @TableField(exist = false)
    private String typeName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 状态 根据type决定
     */
    private Integer state;

    /**
     * 状态 0 未处理 已处理
     */
    private Integer process;

    /**
     * 灯具id，或者 边缘控制器id
     */
    private Long uniqueIdentification;

    /**
     * 1 灯具异常 2 异常停车 3 边缘模组，4串口状态
     */
    private Integer exceptionStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
