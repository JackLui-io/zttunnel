package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.service.DeviceInspectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备巡检控制器
 * 提供无需认证的API接口，供外部定时任务调用
 * 
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/api/tunnel")
public class DeviceInspectionController extends BaseController {
    
    @Autowired
    private DeviceInspectionService deviceInspectionService;
    
    /**
     * 设备巡检接口
     * 外部定时任务调用此接口进行设备健康巡检
     * 注意：此接口采用异步执行，立即返回，实际巡检任务在后台执行
     * 
     * @return 巡检结果
     */
    @PostMapping("/inspection")
    public AjaxResult inspection() {
        log.info("========== 收到设备巡检请求 ==========");
        
        try {
            // 异步执行巡检任务，立即返回，不阻塞HTTP请求线程
            deviceInspectionService.executeInspectionAsync();
            
            // 立即返回，不等待任务完成
            return AjaxResult.success("设备巡检任务已提交，正在后台执行");
        } catch (Exception e) {
            log.error("提交设备巡检任务失败", e);
            return AjaxResult.error("提交设备巡检任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 发送邮件接口
     * 外部定时任务调用此接口发送邮件
     * 
     * @return 发送结果
     */
    @PostMapping("/sendanemail")
    public AjaxResult sendAnEmail() {
        log.info("========== 收到发送邮件请求 ==========");
        
        try {
            // TODO: 实现邮件发送逻辑
            // 1. 查询待发送的邮件（从tunnel_email_queue或tunnel_check_email_history表）
            // 2. 读取邮件内容
            // 3. 发送邮件
            // 4. 记录发送结果到tunnel_check_email_history表
            
            AjaxResult result = deviceInspectionService.sendEmail();
            return result;
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            return AjaxResult.error("发送邮件失败：" + e.getMessage());
        }
    }
    
    /**
     * 每小时检查接口（原 /api/tunnel/inspection）
     * 外部定时任务调用此接口进行设备健康巡检
     * 注意：此接口采用异步执行，立即返回，实际巡检任务在后台执行
     * 
     * @return 巡检结果
     */
    @PostMapping("/check_per_hour")
    public AjaxResult checkPerHour() {
        log.info("========== 收到设备巡检请求（check_per_hour） ==========");
        
        try {
            // 异步执行巡检任务，立即返回，不阻塞HTTP请求线程
            deviceInspectionService.executeInspectionAsync();
            
            // 立即返回，不等待任务完成
            return AjaxResult.success("设备巡检任务已提交，正在后台执行");
        } catch (Exception e) {
            log.error("提交设备巡检任务失败", e);
            return AjaxResult.error("提交设备巡检任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 刷新期望上报间隔表接口
     * 当有设备新增入库时，可以手动调用此接口刷新期望上报间隔表
     * 
     * @return 刷新结果
     */
    @PostMapping("/getexpect_interval")
    public AjaxResult getExpectInterval() {
        log.info("========== 收到刷新期望上报间隔表请求 ==========");
        
        try {
            AjaxResult result = deviceInspectionService.refreshExpectedInterval();
            return result;
        } catch (Exception e) {
            log.error("刷新期望上报间隔表失败", e);
            return AjaxResult.error("刷新期望上报间隔表失败：" + e.getMessage());
        }
    }
}

