package com.scsdky.web.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.config.AdminConfig;
import com.scsdky.web.domain.TunnelCheckRate;
import com.scsdky.web.domain.TunnelDeviceAlarm;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

enum OnlineRateStatus {
    NO_DATA,
    LOW,
    NORMAL
}
/**
 * 健康检查控制器（无需认证）
 * 用于外部系统验证数据库连接状态
 * 
 * @author system
 * @date 2025/12/16
 */
@RestController
@RequestMapping("/api/health")
@Api(value = "健康检查模块", tags = {"健康检查模块（无需认证）"})
public class HealthCheckController extends BaseController {

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;
    
    @Resource
    private TunnelDeviceAlarmService tunnelDeviceAlarmService;
    
    @Resource
    private TunnelCheckRateService tunnelCheckRateService;
    
    @Resource
    private EmailService emailService;
    
    @Resource
    private AdminConfig adminConfig;
    
    @Resource
    private SmsService smsService;

    /**
     * 数据库连接健康检查
     * 查询 tunnel_devicelist 表的最新一条数据
     * 
     * @return 返回最新的设备数据，如果查询成功则说明数据库连接正常
     */
    @GetMapping({"", "/", "/checkAPI"})
    @ApiOperation("数据库连接健康检查")
    public AjaxResult checkDatabaseConnection() {
        try {
            // 查询 tunnel_devicelist 表的最新一条数据（按 lastUpdate 降序）
            LambdaQueryWrapper<TunnelDevicelist> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(TunnelDevicelist::getLastUpdate);
            queryWrapper.last("limit 1");
            
            TunnelDevicelist latestDevice = tunnelDevicelistService.getOne(queryWrapper);
            
            if (latestDevice != null) {
                // 数据库连接正常，返回最新数据
                AjaxResult result = AjaxResult.success("数据库连接正常");
                result.put("status", "healthy");
                result.put("data", latestDevice);
                result.put("timestamp", System.currentTimeMillis());
                return result;
            } else {
                // 数据库连接正常但没有数据
                AjaxResult result = AjaxResult.success("数据库连接正常，但表中暂无数据");
                result.put("status", "healthy");
                result.put("data", null);
                result.put("timestamp", System.currentTimeMillis());
                return result;
            }
        } catch (Exception e) {
            // 数据库连接异常
            logger.error("数据库连接检查失败", e);
            AjaxResult result = AjaxResult.error("数据库连接异常: " + e.getMessage());
            result.put("status", "unhealthy");
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }
    
    /**
     * 构建checkAPI邮件内容（简化版）
     */
    private String buildCheckAPIEmailContent(String status, String message, String checkTime, 
                                             TunnelDevicelist latestDevice, Exception exception) {
        StringBuilder content = new StringBuilder();
        
        if ("healthy".equals(status)) {
            content.append("数据库连接检查 - 正常\n\n");
        } else {
            content.append("数据库连接检查 - 异常\n\n");
        }
        
        content.append("检查时间：").append(checkTime).append("\n");
        content.append("检查状态：").append(status).append("\n");
        content.append("检查结果：").append(message).append("\n");
        
        if (latestDevice != null) {
            content.append("最新设备ID：").append(latestDevice.getDeviceId()).append("\n");
            if (latestDevice.getLastUpdate() != null) {
                content.append("最后更新时间：").append(latestDevice.getLastUpdate()).append("\n");
            }
        }
        
        if (exception != null) {
            content.append("\n错误信息：").append(exception.getMessage()).append("\n");
        }
        
        content.append("\n此邮件由隧道照明智控平台自动发送，请勿回复。");
        
        return content.toString();
    }
    
    /**
     * 检查设备在线率并发送短信告警
     * 查询 tunnel_check_alarm 表最近1小时内的检查结果，计算在线率
     * 如果在线率低于 tunnel_check_rate 表中配置的阈值，则发送短信告警
     * 
     * @return 返回检查结果和在线率信息
     */
    @PostMapping("/checkOnlineRate")
    @ApiOperation("检查设备在线率并发送短信告警")
    public AjaxResult checkOnlineRate() {
        try {
            Date now = new Date();
            Date oneHourAgo = new Date(System.currentTimeMillis() - 60 * 60 * 1000);

            int totalCount = 0;
            long onlineCount = 0;
            double onlineRate = 0.0;

            OnlineRateStatus status;

            // 1. 查询最近 1 小时设备检查记录
            LambdaQueryWrapper<TunnelDeviceAlarm> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(TunnelDeviceAlarm::getUpdateTime, oneHourAgo);
            List<TunnelDeviceAlarm> alarmList = tunnelDeviceAlarmService.list(queryWrapper);

            if (alarmList == null || alarmList.isEmpty()) {
                // 场景 ①：无检查记录
                logger.warn("最近 1 小时内无设备检查记录，在线率记为 0");
                status = OnlineRateStatus.NO_DATA;
            } else {
                // 正常计算在线率
                totalCount = alarmList.size();
                onlineCount = alarmList.stream()
                        .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                        .count();
                onlineRate = (double) onlineCount / totalCount;
                status = OnlineRateStatus.NORMAL; // 先假设正常，后面再比阈值
            }

            // 2. 读取在线率阈值
            LambdaQueryWrapper<TunnelCheckRate> rateQuery = new LambdaQueryWrapper<>();
            rateQuery.orderByDesc(TunnelCheckRate::getId).last("limit 1");
            TunnelCheckRate checkRate = tunnelCheckRateService.getOne(rateQuery);

            if (checkRate == null || checkRate.getRate() == null) {
                logger.error("未配置在线率阈值");
                return AjaxResult.error("未配置在线率阈值");
            }

            double rateThreshold = checkRate.getRate();

            // 3. 判断在线率状态
            if (status != OnlineRateStatus.NO_DATA && onlineRate < rateThreshold) {
                status = OnlineRateStatus.LOW;
            }

            // 4. 只在在线率低于阈值时发送短信
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(now);
            
            int smsSuccess = 0;
            int smsFail = 0;

            // 只在在线率低于阈值时发送短信（包括NO_DATA和LOW状态）
            if (status == OnlineRateStatus.LOW || status == OnlineRateStatus.NO_DATA) {
                List<String> adminPhones = adminConfig.getAdminPhones();
                
                if (adminPhones != null && !adminPhones.isEmpty()) {
                    double onlineRatePercent = status == OnlineRateStatus.NO_DATA ? 0.0 : onlineRate * 100;
                    
                    for (String phone : adminPhones) {
                        if (phone != null && !phone.trim().isEmpty()) {
                            boolean sent = smsService.sendHealthCheckNotification(phone, timeStr, onlineRatePercent);
                            if (sent) smsSuccess++; else smsFail++;
                        }
                    }
                } else {
                    logger.warn("未配置管理员手机号，无法发送短信通知");
                }
            }

            // 5. 返回结果
            AjaxResult result = AjaxResult.success("设备在线率检查完成");
            result.put("totalCount", totalCount);
            result.put("onlineCount", onlineCount);
            result.put("onlineRate", onlineRate);
            result.put("rateThreshold", rateThreshold);
            result.put("status", status.name());
            result.put("smsSuccessCount", smsSuccess);
            result.put("smsFailCount", smsFail);
            result.put("timestamp", System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            logger.error("检查设备在线率失败", e);
            return AjaxResult.error("检查设备在线率失败：" + e.getMessage());
        }
    }


}
