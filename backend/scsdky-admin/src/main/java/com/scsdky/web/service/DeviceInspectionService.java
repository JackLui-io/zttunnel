package com.scsdky.web.service;

import com.scsdky.common.core.domain.AjaxResult;

/**
 * 设备巡检服务接口
 * 
 * @author system
 */
public interface DeviceInspectionService {
    
    /**
     * 执行设备巡检（同步执行，已废弃，建议使用异步方法）
     * 
     * @return 巡检结果
     */
    @Deprecated
    AjaxResult executeInspection();
    
    /**
     * 异步执行设备巡检
     * 立即返回，实际巡检任务在后台执行
     */
    void executeInspectionAsync();
    
    /**
     * 发送邮件
     * 
     * @return 发送结果
     */
    AjaxResult sendEmail();
    
    /**
     * 刷新期望上报间隔表
     * 清空表后重新插入边缘计算终端和灯具终端的数据
     * 
     * @return 刷新结果
     */
    AjaxResult refreshExpectedInterval();
}

