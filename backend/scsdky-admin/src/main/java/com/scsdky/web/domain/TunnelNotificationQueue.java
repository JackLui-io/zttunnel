package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备掉线通知队列表
 * @TableName tunnel_notification_queue
 */
@TableName(value = "tunnel_notification_queue")
@Data
public class TunnelNotificationQueue implements Serializable {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 设备ID
     */
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 设备类型
     */
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 通知类型：1=邮件，2=短信
     */
    @ApiModelProperty(value = "通知类型：1=邮件，2=短信")
    private Integer notificationType;

    /**
     * 收件人邮箱
     */
    @ApiModelProperty(value = "收件人邮箱")
    private String recipientEmail;

    /**
     * 收件人手机号
     */
    @ApiModelProperty(value = "收件人手机号")
    private String recipientPhone;

    /**
     * 收件人用户ID
     */
    @ApiModelProperty(value = "收件人用户ID")
    private Long recipientUserId;

    /**
     * 通知内容
     */
    @ApiModelProperty(value = "通知内容")
    private String content;

    /**
     * 状态：0=待发送，1=发送成功，2=发送失败
     */
    @ApiModelProperty(value = "状态：0=待发送，1=发送成功，2=发送失败")
    private Integer status;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    private Integer retryCount;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发送时间")
    private Date sendTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 通知类型常量
     */
    public static class NotificationType {
        public static final int EMAIL = 1;  // 邮件
        public static final int SMS = 2;    // 短信
    }

    /**
     * 状态常量
     */
    public static class Status {
        public static final int PENDING = 0;    // 待发送
        public static final int SUCCESS = 1;    // 发送成功
        public static final int FAILED = 2;     // 发送失败
    }
}

