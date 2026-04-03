package com.scsdky.web.service.impl;

import com.scsdky.web.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 短信发送服务实现类
 * 注意：此实现为占位实现，需要根据实际使用的短信服务商（阿里云、腾讯云等）进行配置
 * 
 * @author system
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.provider:}")
    private String smsProvider;

    @Value("${sms.smsapi:}")
    private String smsApi;

    @Value("${sms.user:}")
    private String smsUser;

    @Value("${sms.password:}")
    private String smsPassword;

    @Override
    public boolean sendSms(String phone, String content) {
        if (!smsEnabled) {
            log.warn("短信发送服务未启用（sms.enabled=false），无法发送短信到：{}", phone);
            log.warn("如需启用短信发送，请在 application.yml 中配置 sms.enabled=true 并配置短信服务商");
            return false;
        }

        if (phone == null || phone.trim().isEmpty()) {
            log.warn("收件人手机号为空，无法发送短信");
            return false;
        }
        
        if (smsProvider == null || smsProvider.trim().isEmpty()) {
            log.warn("短信服务商未配置（sms.provider为空），无法发送短信到：{}", phone);
            return false;
        }

        try {
            // 使用短信宝API发送短信
            if ("smsbao".equalsIgnoreCase(smsProvider)) {
                if (smsApi != null && !smsApi.trim().isEmpty() && 
                    smsUser != null && !smsUser.trim().isEmpty() && 
                    smsPassword != null && !smsPassword.trim().isEmpty()) {
                    
                    return sendBySmsApi(phone, content);
                } else {
                    log.warn("短信宝配置不完整，无法发送短信到：{}", phone);
                    log.warn("请检查 application.yml 中的 sms.smsapi、sms.user、sms.password 配置");
                    return false;
                }
            }
            // 示例：如果是阿里云短信
            else if ("aliyun".equalsIgnoreCase(smsProvider)) {
                // return sendByAliyun(phone, content);
            }
            // 示例：如果是腾讯云短信
            else if ("tencent".equalsIgnoreCase(smsProvider)) {
                // return sendByTencent(phone, content);
            }
            
            // 默认实现：仅记录日志，不实际发送
            log.info("短信发送（模拟）：收件人={}, 内容={}", phone, content);
            log.warn("短信服务未配置具体实现，请根据实际需求集成短信服务商SDK");
            return true; // 返回true表示已记录，实际使用时需要根据真实发送结果返回
        } catch (Exception e) {
            log.error("短信发送失败：收件人={}", phone, e);
            return false;
        }
    }

    @Override
    public boolean sendDeviceOfflineNotification(String phone, String deviceName, Long deviceId, String deviceType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentTime = sdf.format(new Date());
        
        // 短信内容需要简洁，符合短信长度限制
        String content = String.format("【隧道照明系统】设备掉线通知：设备名称：%s，设备ID：%s，掉线时间：%s。请及时处理。",
                deviceName != null ? deviceName : "未知",
                deviceId,
                currentTime);
        
        return sendSms(phone, content);
    }

    @Override
    public boolean sendHealthCheckNotification(String phone, String checkTime, double onlineRate) {
        // 按照用户要求的短信内容格式
        String content = String.format("【深圳市蓝海互动】尊敬的管理员，在%s的检查中，发现您的设备在线率为%.0f%%，请检查相关程序和设备。",
                checkTime, onlineRate);
        
        return sendSms(phone, content);
    }

    /**
     * 使用短信宝API发送短信
     * 
     * @param phone 手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    private boolean sendBySmsApi(String phone, String content) {
        try {
            // 构建请求URL
            String url = smsApi + "sms";
            
            // URL编码短信内容
            String encodedContent = URLEncoder.encode(content, "UTF-8");
            
            // 处理密码：如果配置中包含md5()，则提取并计算MD5
            String password = processPassword(smsPassword);
            
            // 构建请求参数
            String params = String.format("u=%s&p=%s&m=%s&c=%s", 
                    smsUser, 
                    password, 
                    phone, 
                    encodedContent);
            
            // 发送HTTP请求
            URL requestUrl = new URL(url + "?" + params);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            
            // 读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            
            log.info("短信宝API响应：手机号={}, 响应码={}, 响应内容={}", phone, responseCode, response);
            
            // 短信宝返回码说明：
            // 0: 短信发送成功
            // 30: 密码错误
            // 40: 账号不存在
            // 41: 余额不足
            // 42: 账户已过期
            // 43: IP地址限制
            // 50: 内容含有敏感词
            // 51: 手机号码不正确
            if ("0".equals(response)) {
                log.info("短信发送成功：手机号={}, 内容={}", phone, content);
                return true;
            } else {
                String errorMsg = getErrorMessage(response);
                log.error("短信发送失败：手机号={}, 错误码={}, 错误信息={}", phone, response, errorMsg);
                return false;
            }
            
        } catch (Exception e) {
            log.error("短信宝API调用异常：手机号={}", phone, e);
            return false;
        }
    }
    
    /**
     * 处理密码配置
     * 如果配置格式为 md5('原始密码')，则提取原始密码并计算MD5
     * 否则直接使用配置的值
     */
    private String processPassword(String passwordConfig) {
        if (passwordConfig == null || passwordConfig.trim().isEmpty()) {
            return "";
        }
        
        // 检查是否是 md5('xxx') 格式
        if (passwordConfig.startsWith("md5('") && passwordConfig.endsWith("')")) {
            String originalPassword = passwordConfig.substring(5, passwordConfig.length() - 2);
            return md5(originalPassword);
        }
        
        // 直接使用配置的值
        return passwordConfig;
    }
    
    /**
     * 计算MD5哈希值
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5计算失败", e);
            return input;
        }
    }
    
    /**
     * 根据短信宝错误码获取错误信息
     */
    private String getErrorMessage(String code) {
        switch (code) {
            case "30": return "密码错误";
            case "40": return "账号不存在";
            case "41": return "余额不足";
            case "42": return "账户已过期";
            case "43": return "IP地址限制";
            case "50": return "内容含有敏感词";
            case "51": return "手机号码不正确";
            default: return "未知错误";
        }
    }

    /**
     * 阿里云短信发送（示例方法，需要集成阿里云SDK）
     * 需要添加依赖：com.aliyun:dysmsapi20170525
     */
    /*
    private boolean sendByAliyun(String phone, String content) {
        try {
            // 阿里云短信发送逻辑
            // 需要配置 accessKeyId, accessKeySecret, signName, templateCode 等
            log.info("通过阿里云发送短信：{} -> {}", phone, content);
            return true;
        } catch (Exception e) {
            log.error("阿里云短信发送失败", e);
            return false;
        }
    }
    */

    /**
     * 腾讯云短信发送（示例方法，需要集成腾讯云SDK）
     * 需要添加依赖：com.tencentcloudapi:tencentcloud-sdk-java-sms
     */
    /*
    private boolean sendByTencent(String phone, String content) {
        try {
            // 腾讯云短信发送逻辑
            // 需要配置 secretId, secretKey, appId, signName, templateId 等
            log.info("通过腾讯云发送短信：{} -> {}", phone, content);
            return true;
        } catch (Exception e) {
            log.error("腾讯云短信发送失败", e);
            return false;
        }
    }
    */
}

