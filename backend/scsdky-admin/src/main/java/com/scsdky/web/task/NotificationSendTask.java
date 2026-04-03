package com.scsdky.web.task;

import com.scsdky.web.domain.TunnelNotificationQueue;
import com.scsdky.web.service.EmailService;
import com.scsdky.web.service.SmsService;
import com.scsdky.web.service.TunnelNotificationQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知发送定时任务
 * 每1分钟检查一次待发邮件和短信队列，然后依次发送
 * 
 * @author system
 */
@Slf4j
@Component
public class NotificationSendTask {

    @Resource
    private TunnelNotificationQueueService notificationQueueService;

    @Resource
    private EmailService emailService;

    @Resource
    private SmsService smsService;

    @Value("${notification.batch-size:100}")
    private Integer batchSize;

    @Value("${notification.max-retry-count:3}")
    private Integer maxRetryCount;

    /**
     * 【已关闭】每1分钟检查一次待发邮件和短信队列，然后依次发送
     * cron表达式：0 0/1 * * * ? 表示每分钟的第0秒执行
     * 
     * 注意：此功能已迁移到独立进程健康巡检服务，此处已关闭
     */
    // @Scheduled(cron = "0 0/1 * * * ?")
    // public void sendNotifications() {
    //     executeSendNotifications();
    // }

    /**
     * 执行通知发送（可被启动时立即调用）
     */
    public void executeSendNotifications() {
        log.info("开始执行通知发送任务");
        
        try {
            // 1. 查询待发送的通知记录
            List<TunnelNotificationQueue> pendingNotifications = 
                    notificationQueueService.getPendingNotifications(batchSize);

            if (pendingNotifications == null || pendingNotifications.isEmpty()) {
                log.debug("没有待发送的通知");
                return;
            }

            log.info("找到{}条待发送的通知", pendingNotifications.size());

            // 2. 依次发送通知
            int successCount = 0;
            int failCount = 0;
            
            for (TunnelNotificationQueue notification : pendingNotifications) {
                // 检查重试次数
                if (notification.getRetryCount() != null && 
                    notification.getRetryCount() >= maxRetryCount) {
                    log.warn("通知重试次数已达上限，跳过：ID={}, 设备ID={}, 类型={}", 
                            notification.getId(), 
                            notification.getDeviceId(), 
                            notification.getNotificationType());
                    // 标记为失败
                    notificationQueueService.updateNotificationStatus(
                            notification.getId(),
                            TunnelNotificationQueue.Status.FAILED,
                            "重试次数已达上限：" + maxRetryCount
                    );
                    failCount++;
                    continue;
                }

                // 根据通知类型发送
                boolean success = false;
                String errorMessage = null;

                try {
                    if (notification.getNotificationType() == TunnelNotificationQueue.NotificationType.EMAIL) {
                        // 发送邮件
                        if (notification.getRecipientEmail() == null || notification.getRecipientEmail().trim().isEmpty()) {
                            errorMessage = "收件人邮箱为空";
                            log.warn("邮件通知失败：ID={}, 错误={}", notification.getId(), errorMessage);
                        } else {
                            success = emailService.sendDeviceOfflineNotification(
                                    notification.getRecipientEmail(),
                                    notification.getDeviceName(),
                                    notification.getDeviceId(),
                                    notification.getDeviceType()
                            );
                            if (!success) {
                                errorMessage = "邮件发送服务返回失败（可能未配置邮件服务或发送失败）";
                            }
                        }
                    } else if (notification.getNotificationType() == TunnelNotificationQueue.NotificationType.SMS) {
                        // 发送短信（已暂停，暂时不使用短信通知）
                        // TODO: 如需启用短信通知，请取消下面的注释
                        /*
                        if (notification.getRecipientPhone() == null || notification.getRecipientPhone().trim().isEmpty()) {
                            errorMessage = "收件人手机号为空";
                            log.warn("短信通知失败：ID={}, 错误={}", notification.getId(), errorMessage);
                        } else {
                            success = smsService.sendDeviceOfflineNotification(
                                    notification.getRecipientPhone(),
                                    notification.getDeviceName(),
                                    notification.getDeviceId(),
                                    notification.getDeviceType()
                            );
                            if (!success) {
                                errorMessage = "短信发送服务返回失败（可能未启用短信服务或发送失败）";
                            }
                        }
                        */
                        // 短信服务已暂停，直接标记为成功，不实际发送
                        log.info("短信通知服务已暂停，跳过发送：ID={}, 收件人={}", 
                                notification.getId(), notification.getRecipientPhone());
                        success = true; // 模拟发送成功
                        errorMessage = "短信通知服务已暂停，未实际发送";
                    } else {
                        errorMessage = "未知的通知类型：" + notification.getNotificationType();
                        log.warn("通知失败：ID={}, 错误={}", notification.getId(), errorMessage);
                    }
                } catch (Exception e) {
                    success = false;
                    errorMessage = "发送异常：" + e.getMessage();
                    log.error("发送通知异常：ID={}, 类型={}", 
                            notification.getId(), 
                            notification.getNotificationType(), 
                            e);
                }

                // 3. 更新发送状态
                if (success) {
                    notificationQueueService.updateNotificationStatus(
                            notification.getId(),
                            TunnelNotificationQueue.Status.SUCCESS,
                            null
                    );
                    successCount++;
                    log.info("通知发送成功：ID={}, 类型={}, 收件人={}", 
                            notification.getId(),
                            notification.getNotificationType(),
                            notification.getNotificationType() == TunnelNotificationQueue.NotificationType.EMAIL 
                                    ? notification.getRecipientEmail() 
                                    : notification.getRecipientPhone());
                } else {
                    // 发送失败处理
                    // 注意：短信服务已暂停，不会走到这里
                    // 邮件发送失败：保持待发送状态，允许重试（直到达到最大重试次数）
                    notificationQueueService.updateNotificationStatus(
                            notification.getId(),
                            TunnelNotificationQueue.Status.PENDING, // 保持待发送状态，以便重试
                            errorMessage
                    );
                    failCount++;
                    log.warn("邮件通知发送失败（将重试）：ID={}, 类型={}, 错误={}", 
                            notification.getId(),
                            notification.getNotificationType(),
                            errorMessage);
                }
            }

            log.info("通知发送任务完成，成功：{}条，失败：{}条", successCount, failCount);
        } catch (Exception e) {
            log.error("通知发送任务执行失败", e);
        }
    }
}

