package com.scsdky.web.service.impl;

import com.scsdky.web.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 邮件发送服务实现类
 * 
 * @author system
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.from:}")
    private String from;

    @Override
    public boolean sendEmail(String to, String subject, String content) {
        if (mailSender == null) {
            log.error("邮件发送服务未配置（JavaMailSender为null），无法发送邮件到：{}", to);
            log.error("请检查：1. spring-boot-starter-mail 依赖是否已添加；2. spring.mail 配置是否正确");
            return false;
        }

        if (to == null || to.trim().isEmpty()) {
            log.warn("收件人邮箱为空，无法发送邮件");
            return false;
        }

        // 检查发件人配置
        String sender = from != null && !from.isEmpty() ? from : fromEmail;
        if (sender == null || sender.trim().isEmpty()) {
            log.error("发件人邮箱未配置（spring.mail.from 或 spring.mail.username 为空），无法发送邮件");
            return false;
        }

        try {
            log.debug("开始发送邮件：发件人={}, 收件人={}, 主题={}", sender, to, subject);
            
            // 使用HTML格式发送邮件
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示支持HTML格式
            
            mailSender.send(message);
            log.info("邮件发送成功：发件人={}, 收件人={}, 主题={}", sender, to, subject);
            return true;
        } catch (javax.mail.AuthenticationFailedException e) {
            log.error("邮件发送失败：认证失败，请检查用户名和密码是否正确 - 收件人={}, 主题={}", to, subject, e);
            return false;
        } catch (javax.mail.MessagingException e) {
            log.error("邮件发送失败：邮件服务器连接或配置错误 - 收件人={}, 主题={}, 错误信息={}", to, subject, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("邮件发送失败：未知错误 - 收件人={}, 主题={}", to, subject, e);
            return false;
        }
    }

    @Override
    public boolean sendDeviceOfflineNotification(String to, String deviceName, Long deviceId, String deviceType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        
        String subject = "【设备掉线通知】隧道照明智控平台";
        String content = buildDeviceOfflineEmailContent(deviceName, deviceId, deviceType, currentTime);
        
        return sendEmail(to, subject, content);
    }

    /**
     * 构建设备掉线邮件内容
     */
    private String buildDeviceOfflineEmailContent(String deviceName, Long deviceId, String deviceType, String offlineTime) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #f44336; color: white; padding: 20px; text-align: center; }");
        html.append(".content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; }");
        html.append(".info-item { margin: 10px 0; }");
        html.append(".label { font-weight: bold; color: #666; }");
        html.append(".footer { margin-top: 20px; padding: 20px; text-align: center; color: #999; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h2>设备掉线通知</h2>");
        html.append("</div>");
        html.append("<div class='content'>");
        html.append("<p>尊敬的管理员：</p>");
        html.append("<p>系统检测到以下设备已掉线，请及时处理：</p>");
        html.append("<div class='info-item'><span class='label'>设备名称：</span>").append(deviceName != null ? deviceName : "未知").append("</div>");
        html.append("<div class='info-item'><span class='label'>设备ID：</span>").append(deviceId).append("</div>");
        html.append("<div class='info-item'><span class='label'>设备类型：</span>").append(deviceType != null ? deviceType : "未知").append("</div>");
        html.append("<div class='info-item'><span class='label'>掉线时间：</span>").append(offlineTime).append("</div>");
        html.append("<p style='margin-top: 20px;'>请登录系统查看详细信息并采取相应措施。</p>");
        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>此邮件由隧道照明智控平台自动发送，请勿回复。</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }
}

