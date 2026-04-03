package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelNotificationQueue;

import java.util.List;

/**
 * 设备掉线通知队列服务接口
 * 
 * @author system
 */
public interface TunnelNotificationQueueService extends IService<TunnelNotificationQueue> {
    
    /**
     * 添加通知到队列
     * 
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param notificationType 通知类型（1=邮件，2=短信）
     * @param recipientEmail 收件人邮箱
     * @param recipientPhone 收件人手机号
     * @param recipientUserId 收件人用户ID
     * @param content 通知内容
     * @return 是否成功
     */
    boolean addNotificationToQueue(Long deviceId, String deviceName, String deviceType,
                                     Integer notificationType, String recipientEmail, 
                                     String recipientPhone, Long recipientUserId, String content);
    
    /**
     * 查询待发送的通知记录
     * 
     * @param limit 限制数量
     * @return 通知记录列表
     */
    List<TunnelNotificationQueue> getPendingNotifications(Integer limit);
    
    /**
     * 更新通知发送状态
     * 
     * @param id 通知ID
     * @param status 状态（0=待发送，1=发送成功，2=发送失败）
     * @param errorMessage 错误信息（可选）
     * @return 是否成功
     */
    boolean updateNotificationStatus(Long id, Integer status, String errorMessage);
    
    /**
     * 检查是否已有未发送的通知（防重复）
     * 
     * @param deviceId 设备ID
     * @param notificationType 通知类型
     * @param recipientEmail 收件人邮箱（邮件通知时使用）
     * @param recipientUserId 收件人用户ID（可选）
     * @return 是否存在
     */
    boolean hasPendingNotification(Long deviceId, Integer notificationType, String recipientEmail, Long recipientUserId);

    /**
     * 检查指定时间范围内是否已发送过通知（防重复）
     * 
     * @param deviceId 设备ID
     * @param notificationType 通知类型
     * @param recipientEmail 收件人邮箱（邮件通知时使用）
     * @param recipientUserId 收件人用户ID（可选）
     * @param hours 时间范围（小时）
     * @return 是否在指定时间范围内已发送过通知
     */
    boolean hasRecentNotification(Long deviceId, Integer notificationType, String recipientEmail, Long recipientUserId, Integer hours);
}

