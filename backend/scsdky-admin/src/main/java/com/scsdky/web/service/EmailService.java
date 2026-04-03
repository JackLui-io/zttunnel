package com.scsdky.web.service;

/**
 * 邮件发送服务接口
 * 
 * @author system
 */
public interface EmailService {
    
    /**
     * 发送邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean sendEmail(String to, String subject, String content);
    
    /**
     * 发送设备掉线通知邮件
     * 
     * @param to 收件人邮箱
     * @param deviceName 设备名称
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @return 是否发送成功
     */
    boolean sendDeviceOfflineNotification(String to, String deviceName, Long deviceId, String deviceType);
}

