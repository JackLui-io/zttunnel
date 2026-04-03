package com.scsdky.web.service;

/**
 * 短信发送服务接口
 * 
 * @author system
 */
public interface SmsService {
    
    /**
     * 发送短信
     * 
     * @param phone 收件人手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String content);
    
    /**
     * 发送设备掉线通知短信
     * 
     * @param phone 收件人手机号
     * @param deviceName 设备名称
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @return 是否发送成功
     */
    boolean sendDeviceOfflineNotification(String phone, String deviceName, Long deviceId, String deviceType);
    
    /**
     * 发送设备健康检查短信
     * 
     * @param phone 收件人手机号
     * @param checkTime 检查时间
     * @param onlineRate 在线率百分比
     * @return 是否发送成功
     */
    boolean sendHealthCheckNotification(String phone, String checkTime, double onlineRate);
}

