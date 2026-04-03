package com.scsdky.web.service.impl;

import com.scsdky.common.core.redis.RedisCache;
import com.scsdky.web.service.SchedulerConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据汇总定时任务配置服务实现
 * 配置存储在 Redis，Redis 无值时从 application.yml 初始化并写入
 */
@Service
public class SchedulerConfigServiceImpl implements SchedulerConfigService {

    private static final String REDIS_KEY_PREFIX = "scheduler:";

    private static final String KEY_SYNC_TRAFFIC_FLOW_DAY = "sync-traffic-flow-day";
    private static final String KEY_SYNC_INSIDE_OUTSIDE_DAY = "sync-inside-outside-day";
    private static final String KEY_SYNC_CARBON_DAY = "sync-carbon-day";
    private static final String KEY_CHECK_POWER_DATA = "check-power-data";

    @Resource
    private RedisCache redisCache;

    @Value("${scheduler.sync-traffic-flow-day:false}")
    private boolean defaultSyncTrafficFlowDay;
    @Value("${scheduler.sync-inside-outside-day:false}")
    private boolean defaultSyncInsideOutsideDay;
    @Value("${scheduler.sync-carbon-day:false}")
    private boolean defaultSyncCarbonDay;
    @Value("${scheduler.check-power-data:false}")
    private boolean defaultCheckPowerData;

    @PostConstruct
    public void init() {
        // 启动时若 Redis 无值，用 yml 默认值初始化
        seedIfAbsent(KEY_SYNC_TRAFFIC_FLOW_DAY, defaultSyncTrafficFlowDay);
        seedIfAbsent(KEY_SYNC_INSIDE_OUTSIDE_DAY, defaultSyncInsideOutsideDay);
        seedIfAbsent(KEY_SYNC_CARBON_DAY, defaultSyncCarbonDay);
        seedIfAbsent(KEY_CHECK_POWER_DATA, defaultCheckPowerData);
    }

    private void seedIfAbsent(String key, boolean value) {
        String redisKey = REDIS_KEY_PREFIX + key;
        if (redisCache.getCacheObject(redisKey) == null) {
            redisCache.setCacheObject(redisKey, value);
        }
    }

    private boolean getFromRedis(String key) {
        String redisKey = REDIS_KEY_PREFIX + key;
        Object val = redisCache.getCacheObject(redisKey);
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return "true".equalsIgnoreCase(String.valueOf(val));
    }

    private void setToRedis(String key, boolean value) {
        redisCache.setCacheObject(REDIS_KEY_PREFIX + key, value);
    }

    /**
     * taskKey 到 Redis key 的映射
     */
    private String taskKeyToRedisKey(String taskKey) {
        switch (taskKey) {
            case "syncTrafficFlowDay":
                return KEY_SYNC_TRAFFIC_FLOW_DAY;
            case "syncInsideOutsideDay":
                return KEY_SYNC_INSIDE_OUTSIDE_DAY;
            case "syncCarbonDay":
                return KEY_SYNC_CARBON_DAY;
            case "checkPowerData":
                return KEY_CHECK_POWER_DATA;
            default:
                return null;
        }
    }

    @Override
    public Map<String, Boolean> getStatus() {
        Map<String, Boolean> map = new HashMap<>(4);
        map.put("syncTrafficFlowDay", getFromRedis(KEY_SYNC_TRAFFIC_FLOW_DAY));
        map.put("syncInsideOutsideDay", getFromRedis(KEY_SYNC_INSIDE_OUTSIDE_DAY));
        map.put("syncCarbonDay", getFromRedis(KEY_SYNC_CARBON_DAY));
        map.put("checkPowerData", getFromRedis(KEY_CHECK_POWER_DATA));
        return map;
    }

    @Override
    public void setEnabled(String taskKey, boolean enabled) {
        String redisKey = taskKeyToRedisKey(taskKey);
        if (redisKey != null) {
            setToRedis(redisKey, enabled);
        }
    }

    @Override
    public boolean isSyncTrafficFlowDayEnabled() {
        return getFromRedis(KEY_SYNC_TRAFFIC_FLOW_DAY);
    }

    @Override
    public boolean isSyncInsideOutsideDayEnabled() {
        return getFromRedis(KEY_SYNC_INSIDE_OUTSIDE_DAY);
    }

    @Override
    public boolean isSyncCarbonDayEnabled() {
        return getFromRedis(KEY_SYNC_CARBON_DAY);
    }

    @Override
    public boolean isCheckPowerDataEnabled() {
        return getFromRedis(KEY_CHECK_POWER_DATA);
    }
}
