package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelNotificationQueue;
import com.scsdky.web.mapper.TunnelNotificationQueueMapper;
import com.scsdky.web.service.TunnelNotificationQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 设备掉线通知队列服务实现类
 * 
 * @author system
 */
@Slf4j
@Service
public class TunnelNotificationQueueServiceImpl extends ServiceImpl<TunnelNotificationQueueMapper, TunnelNotificationQueue> 
        implements TunnelNotificationQueueService {

    @Resource
    private TunnelNotificationQueueMapper notificationQueueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addNotificationToQueue(Long deviceId, String deviceName, String deviceType,
                                          Integer notificationType, String recipientEmail,
                                          String recipientPhone, Long recipientUserId, String content) {
        try {
            TunnelNotificationQueue queue = new TunnelNotificationQueue();
            queue.setDeviceId(deviceId);
            queue.setDeviceName(deviceName);
            queue.setDeviceType(deviceType);
            queue.setNotificationType(notificationType);
            queue.setRecipientEmail(recipientEmail);
            queue.setRecipientPhone(recipientPhone);
            queue.setRecipientUserId(recipientUserId);
            queue.setContent(content);
            queue.setStatus(TunnelNotificationQueue.Status.PENDING);
            queue.setRetryCount(0);
            queue.setCreateTime(new Date());
            
            boolean result = save(queue);
            if (result) {
                log.info("添加通知到队列成功：设备ID={}, 通知类型={}, 收件人={}", 
                        deviceId, notificationType, 
                        notificationType == TunnelNotificationQueue.NotificationType.EMAIL ? recipientEmail : recipientPhone);
            }
            return result;
        } catch (Exception e) {
            log.error("添加通知到队列失败：设备ID={}, 通知类型={}", deviceId, notificationType, e);
            return false;
        }
    }

    @Override
    public List<TunnelNotificationQueue> getPendingNotifications(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100; // 默认每次最多处理100条
        }
        // 查询待发送的通知记录（status=0）
        // 注意：这里只查询待发送的，防重复检查在添加通知时已经完成
        return notificationQueueMapper.selectPendingNotifications(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotificationStatus(Long id, Integer status, String errorMessage) {
        try {
            TunnelNotificationQueue queue = getById(id);
            if (queue == null) {
                log.warn("通知记录不存在：ID={}", id);
                return false;
            }
            
            queue.setStatus(status);
            if (status == TunnelNotificationQueue.Status.SUCCESS) {
                queue.setSendTime(new Date());
            } else if (status == TunnelNotificationQueue.Status.FAILED) {
                queue.setRetryCount(queue.getRetryCount() + 1);
                queue.setErrorMessage(errorMessage);
            }
            
            boolean result = updateById(queue);
            if (result) {
                log.info("更新通知状态成功：ID={}, 状态={}", id, status);
            }
            return result;
        } catch (Exception e) {
            log.error("更新通知状态失败：ID={}, 状态={}", id, status, e);
            return false;
        }
    }

    @Override
    public boolean hasPendingNotification(Long deviceId, Integer notificationType, String recipientEmail, Long recipientUserId) {
        LambdaQueryWrapper<TunnelNotificationQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TunnelNotificationQueue::getDeviceId, deviceId)
               .eq(TunnelNotificationQueue::getNotificationType, notificationType)
               .eq(TunnelNotificationQueue::getStatus, TunnelNotificationQueue.Status.PENDING);
        
        // 根据通知类型添加收件人条件
        if (notificationType == TunnelNotificationQueue.NotificationType.EMAIL) {
            // 邮件通知：检查邮箱
            if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
                wrapper.eq(TunnelNotificationQueue::getRecipientEmail, recipientEmail);
            } else if (recipientUserId != null) {
                wrapper.eq(TunnelNotificationQueue::getRecipientUserId, recipientUserId);
            }
        } else if (notificationType == TunnelNotificationQueue.NotificationType.SMS) {
            // 短信通知：检查用户ID或手机号
            if (recipientUserId != null) {
                wrapper.eq(TunnelNotificationQueue::getRecipientUserId, recipientUserId);
            }
        }
        
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasRecentNotification(Long deviceId, Integer notificationType, String recipientEmail, Long recipientUserId, Integer hours) {
        if (hours == null || hours <= 0) {
            return false;
        }
        
        // 计算时间范围：当前时间往前推N小时
        Date timeThreshold = new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000L);
        
        LambdaQueryWrapper<TunnelNotificationQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TunnelNotificationQueue::getDeviceId, deviceId)
               .eq(TunnelNotificationQueue::getNotificationType, notificationType)
               .eq(TunnelNotificationQueue::getStatus, TunnelNotificationQueue.Status.SUCCESS)  // 只检查已成功发送的
               .ge(TunnelNotificationQueue::getSendTime, timeThreshold);  // 发送时间在指定时间范围内
        
        // 根据通知类型添加收件人条件
        if (notificationType == TunnelNotificationQueue.NotificationType.EMAIL) {
            // 邮件通知：检查邮箱
            if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
                wrapper.eq(TunnelNotificationQueue::getRecipientEmail, recipientEmail);
            } else if (recipientUserId != null) {
                wrapper.eq(TunnelNotificationQueue::getRecipientUserId, recipientUserId);
            }
        } else if (notificationType == TunnelNotificationQueue.NotificationType.SMS) {
            // 短信通知：检查用户ID或手机号
            if (recipientUserId != null) {
                wrapper.eq(TunnelNotificationQueue::getRecipientUserId, recipientUserId);
            }
        }
        
        return count(wrapper) > 0;
    }
}

