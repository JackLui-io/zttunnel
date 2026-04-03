package com.scsdky.web.service;

import java.util.Map;

/**
 * 数据汇总定时任务配置服务
 * 配置存储在 Redis，支持运行时修改，重启后持久化
 */
public interface SchedulerConfigService {

    /**
     * 获取各任务开关状态
     */
    Map<String, Boolean> getStatus();

    /**
     * 更新任务开关状态
     * @param taskKey syncTrafficFlowDay | syncInsideOutsideDay | syncCarbonDay | checkPowerData
     */
    void setEnabled(String taskKey, boolean enabled);

    boolean isSyncTrafficFlowDayEnabled();
    boolean isSyncInsideOutsideDayEnabled();
    boolean isSyncCarbonDayEnabled();
    boolean isCheckPowerDataEnabled();
}
