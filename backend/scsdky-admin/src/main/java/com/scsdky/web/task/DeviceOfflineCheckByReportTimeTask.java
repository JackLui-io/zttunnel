package com.scsdky.web.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.core.domain.entity.SysUser;
import com.scsdky.web.config.AdminConfig;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.domain.TunnelDeviceParam;
import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.scsdky.web.domain.TunnelNotificationQueue;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelDeviceParamService;
import com.scsdky.web.service.TunnelEdgeComputeDataService;
import com.scsdky.web.service.TunnelNotificationQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 基于实际上报时间的设备掉线检测定时任务
 * 
 * 功能说明：
 * 1. 每35分钟检查一次边缘控制器设备的上报情况
 * 2. 查询每个设备最新上报的一条数据
 * 3. 如果设备配置了上报间隔，使用配置值；否则使用默认值（35分钟）
 * 4. 如果最新上报时间距离当前时间超过（上报间隔 + 容错时间），则判断为掉线
 * 5. 更新设备状态为离线，并发送邮件通知给管理员
 * 
 * 优势：
 * - 不依赖数据库中的状态字段，而是基于实际上报数据的时间来判断
 * - 支持不同设备有不同的上报间隔配置
 * - 更加准确和可靠
 * 
 * @author system
 */
@Slf4j
@Component
public class DeviceOfflineCheckByReportTimeTask {

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;

    @Resource
    private TunnelDeviceParamService tunnelDeviceParamService;

    @Resource
    private TunnelNotificationQueueService notificationQueueService;

    @Autowired
    private AdminConfig adminConfig;

    /**
     * 默认上报间隔（分钟），当设备未配置上报间隔时使用
     */
    @Value("${device.offline-check.default-report-interval-minutes:35}")
    private Integer defaultReportIntervalMinutes;

    /**
     * 容错时间（分钟），允许设备延迟上报的时间
     * 例如：设备配置5分钟上报一次，容错5分钟，则超过10分钟未上报才判断为掉线
     */
    @Value("${device.offline-check.tolerance-minutes:5}")
    private Integer toleranceMinutes;

    /**
     * 防重复通知时间（小时），默认30小时
     */
    @Value("${notification.duplicate-check-hours:30}")
    private Integer duplicateCheckHours;

    /**
     * 查询数据的时间范围（小时），只查询最近N小时内的上报数据
     * 避免查询到上个月甚至上半年的数据，提高查询性能
     * 默认2小时，如果设备2小时内都没有上报数据，说明可能掉线了
     */
    @Value("${device.offline-check.query-hours:2}")
    private Integer queryHours;

    // 管理员用户缓存
    private List<SysUser> cachedAdminUsers = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟

    /**
     * 【已关闭】应用启动时立即执行一次设备掉线检查
     * 使用 ApplicationReadyEvent 确保所有 Bean 都已初始化完成
     * 
     * 注意：此功能已迁移到独立进程健康巡检服务，此处已关闭
     */
    // @EventListener(ApplicationReadyEvent.class)
    // public void onApplicationReady() {
    //     log.info("========== 应用启动完成，立即执行一次设备掉线检查 ==========");
    //     // 延迟5秒执行，确保所有服务都已完全启动
    //     try {
    //         Thread.sleep(5000);
    //     } catch (InterruptedException e) {
    //         Thread.currentThread().interrupt();
    //         log.warn("[启动检查] 延迟执行被中断", e);
    //     }
    //     executeCheckDeviceOffline();
    // }

    /**
     * 【已关闭】每35分钟检查一次设备掉线情况
     * cron表达式：0 0/35 * * * ? 表示每35分钟的第0秒执行
     * 
     * 注意：此功能已迁移到独立进程健康巡检服务，此处已关闭
     */
    // @Scheduled(cron = "0 0/35 * * * ?")
    // public void checkDeviceOfflineByReportTime() {
    //     executeCheckDeviceOffline();
    // }

    /**
     * 执行设备掉线检查（可被启动时立即调用）
     */
    private void executeCheckDeviceOffline() {
        log.info("========== 开始执行基于上报时间的设备掉线检查任务 ==========");
        
        try {
            // 查询所有边缘控制器设备（deviceTypeId=1）
            List<TunnelDevicelist> edgeDevices = tunnelDevicelistService.list(
                    new LambdaQueryWrapper<TunnelDevicelist>()
                            .eq(TunnelDevicelist::getDeviceTypeId, 1)
            );

            if (edgeDevices == null || edgeDevices.isEmpty()) {
                log.warn("[设备掉线检查] 未找到边缘控制器设备");
                return;
            }

            log.info("[设备掉线检查] 查询到 {} 个边缘控制器设备，开始检查...", edgeDevices.size());

            // 获取管理员用户列表（用于发送通知）
            List<SysUser> adminUsers = getAdminUsersWithCache();
            if (adminUsers == null || adminUsers.isEmpty()) {
                log.warn("[设备掉线检查] 未找到管理员用户，无法发送通知");
            }

            int offlineCount = 0;
            int onlineCount = 0;
            int noDataCount = 0;
            int skipNotificationCount = 0; // 跳过通知的数量（已发送过）
            Date currentTime = new Date();

            // 计算查询时间范围：只查询最近N小时内的数据
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -queryHours);
            Date queryTimeThreshold = cal.getTime();
            
            log.info("[设备掉线检查] 查询时间范围：只查询最近 {} 小时内的上报数据（{} 之后的数据）", 
                    queryHours, queryTimeThreshold);

            // 检查每个设备
            for (TunnelDevicelist device : edgeDevices) {
                Long deviceId = device.getDeviceId();
                String deviceName = device.getNickName() != null ? device.getNickName() : "设备" + deviceId;

                try {
                    // ========== 优化：限制查询时间范围，只查询最近N天内的数据 ==========
                    // 避免查询到上个月甚至上半年的数据，提高查询性能
                    // 如果设备在最近N天内都没有上报数据，说明早就掉线了，不应该重复发送通知
                    TunnelEdgeComputeData latestData = tunnelEdgeComputeDataService.getOne(
                            new LambdaQueryWrapper<TunnelEdgeComputeData>()
                                    .eq(TunnelEdgeComputeData::getDevicelistId, deviceId)
                                    .ge(TunnelEdgeComputeData::getUploadTime, queryTimeThreshold)  // 只查询最近N天内的数据
                                    .orderByDesc(TunnelEdgeComputeData::getUploadTime)
                                    .last("limit 1")
                    );

                    if (latestData == null || latestData.getUploadTime() == null) {
                        log.warn("[设备掉线检查] 设备ID: {}, 名称: {}, 状态: 最近{}小时内无上报数据", 
                                deviceId, deviceName, queryHours);
                        noDataCount++;
                        
                        // ========== 优化：如果设备在最近N小时内没有上报数据，检查是否已经发送过通知 ==========
                        // 避免重复发送通知（设备早就掉线了，不应该每次都发送）
                        boolean shouldNotify = shouldSendNotification(deviceId, adminUsers);
                        if (shouldNotify) {
                            updateDeviceOfflineStatus(device, deviceName, adminUsers, 
                                    String.format("最近%d小时内无上报数据", queryHours));
                            offlineCount++;
                        } else {
                            log.info("[设备掉线检查] 设备ID: {}, 名称: {}, 最近{}小时内无上报数据，但已发送过通知，跳过", 
                                    deviceId, deviceName, queryHours);
                            skipNotificationCount++;
                            // 仍然更新设备状态为离线
                            if (device.getOnline() == null || device.getOnline() != 1) {
                                device.setOnline(1);
                                tunnelDevicelistService.updateById(device);
                            }
                        }
                        continue;
                    }

                    Date lastUploadTime = latestData.getUploadTime();
                    
                    // 计算时间差（分钟）
                    long timeDiffMinutes = (currentTime.getTime() - lastUploadTime.getTime()) / (60 * 1000);

                    // 获取设备配置的上报间隔（分钟）
                    Integer reportIntervalMinutes = getDeviceReportInterval(deviceId);
                    
                    // 计算超时阈值：上报间隔 + 容错时间
                    int timeoutThreshold = reportIntervalMinutes + toleranceMinutes;

                    log.debug("[设备掉线检查] 设备ID: {}, 名称: {}, 最新上报时间: {}, 时间差: {} 分钟, 上报间隔: {} 分钟, 超时阈值: {} 分钟",
                            deviceId, deviceName, lastUploadTime, timeDiffMinutes, reportIntervalMinutes, timeoutThreshold);

                    // 判断是否掉线
                    if (timeDiffMinutes > timeoutThreshold) {
                        log.warn("[设备掉线检查] 设备ID: {}, 名称: {}, 状态: 掉线 - 最新上报时间: {}, 距离当前时间: {} 分钟, 超过阈值: {} 分钟",
                                deviceId, deviceName, lastUploadTime, timeDiffMinutes, timeoutThreshold);
                        
                        // ========== 优化：检查是否应该发送通知 ==========
                        // 避免重复发送通知
                        boolean shouldNotify = shouldSendNotification(deviceId, adminUsers);
                        if (shouldNotify) {
                            // 更新设备状态为离线，并发送通知
                            updateDeviceOfflineStatus(device, deviceName, adminUsers, 
                                    String.format("最新上报时间: %s, 距离当前时间: %d 分钟", lastUploadTime, timeDiffMinutes));
                            offlineCount++;
                        } else {
                            log.info("[设备掉线检查] 设备ID: {}, 名称: {}, 状态: 掉线，但已发送过通知，跳过发送", 
                                    deviceId, deviceName);
                            skipNotificationCount++;
                            // 仍然更新设备状态为离线
                            if (device.getOnline() == null || device.getOnline() != 1) {
                                device.setOnline(1);
                                tunnelDevicelistService.updateById(device);
                            }
                        }
                    } else {
                        log.debug("[设备掉线检查] 设备ID: {}, 名称: {}, 状态: 在线 - 最新上报时间: {}, 距离当前时间: {} 分钟",
                                deviceId, deviceName, lastUploadTime, timeDiffMinutes);
                        
                        // 更新设备状态为在线（如果之前是离线状态）
                        if (device.getOnline() != null && device.getOnline() == 1) {
                            device.setOnline(0);
                            tunnelDevicelistService.updateById(device);
                            log.info("[设备掉线检查] 设备ID: {}, 名称: {}, 状态已更新为在线", deviceId, deviceName);
                        }
                        onlineCount++;
                    }

                } catch (Exception e) {
                    log.error("[设备掉线检查] 检查设备失败 - 设备ID: {}, 名称: {}", deviceId, deviceName, e);
                }
            }

            log.info("========== 设备掉线检查任务完成 ==========");
            log.info("统计结果 - 在线: {} 个, 掉线: {} 个, 无数据: {} 个, 跳过通知: {} 个, 总计: {} 个",
                    onlineCount, offlineCount, noDataCount, skipNotificationCount, edgeDevices.size());

        } catch (Exception e) {
            log.error("========== 设备掉线检查任务执行失败 ==========", e);
        }
    }

    /**
     * 获取设备配置的上报间隔（分钟）
     * 如果设备未配置，返回默认值
     */
    private Integer getDeviceReportInterval(Long deviceId) {
        try {
            TunnelDeviceParam deviceParam = tunnelDeviceParamService.getOne(
                    new LambdaQueryWrapper<TunnelDeviceParam>()
                            .eq(TunnelDeviceParam::getDevicelistId, deviceId)
            );

            if (deviceParam != null && deviceParam.getEdgeStatusReportInterval() != null) {
                // 配置的上报间隔是秒，转换为分钟
                int intervalSeconds = deviceParam.getEdgeStatusReportInterval();
                int intervalMinutes = intervalSeconds / 60;
                
                // 如果转换后为0，至少返回1分钟
                if (intervalMinutes <= 0) {
                    intervalMinutes = 1;
                }
                
                log.debug("[设备参数] 设备ID: {}, 配置的上报间隔: {} 秒 ({} 分钟)", 
                        deviceId, intervalSeconds, intervalMinutes);
                return intervalMinutes;
            }
        } catch (Exception e) {
            log.warn("[设备参数] 查询设备参数失败 - 设备ID: {}, 将使用默认值", deviceId, e);
        }

        log.debug("[设备参数] 设备ID: {}, 未配置上报间隔，使用默认值: {} 分钟", deviceId, defaultReportIntervalMinutes);
        return defaultReportIntervalMinutes;
    }

    /**
     * 判断是否应该发送通知
     * 检查是否已有待发送的通知，或最近N小时内已成功发送过通知
     * 
     * @param deviceId 设备ID
     * @param adminUsers 管理员用户列表
     * @return true=应该发送通知，false=不应该发送（已发送过）
     */
    private boolean shouldSendNotification(Long deviceId, List<SysUser> adminUsers) {
        if (adminUsers == null || adminUsers.isEmpty()) {
            return false; // 没有管理员，不发送
        }
        
        // 检查每个管理员是否应该发送通知
        // 只要有一个管理员需要发送通知，就返回true
        for (SysUser admin : adminUsers) {
            if (admin.getEmail() != null && !admin.getEmail().trim().isEmpty()) {
                // 检查是否有待发送的通知
                boolean hasPending = notificationQueueService.hasPendingNotification(
                        deviceId, 
                        TunnelNotificationQueue.NotificationType.EMAIL,
                        admin.getEmail(),
                        admin.getUserId());
                
                // 检查最近N小时内是否已成功发送过通知
                boolean hasRecent = notificationQueueService.hasRecentNotification(
                        deviceId,
                        TunnelNotificationQueue.NotificationType.EMAIL,
                        admin.getEmail(),
                        admin.getUserId(),
                        duplicateCheckHours);
                
                // 如果这个管理员没有待发送的通知，且最近N小时内没有发送过，则需要发送
                if (!hasPending && !hasRecent) {
                    return true;
                }
            }
        }
        
        // 所有管理员都已发送过通知，不需要再发送
        return false;
    }

    /**
     * 更新设备状态为离线，并发送通知
     */
    private void updateDeviceOfflineStatus(TunnelDevicelist device, String deviceName, 
                                          List<SysUser> adminUsers, String reason) {
        try {
            // 更新设备状态为离线（Online=1表示离线）
            if (device.getOnline() == null || device.getOnline() != 1) {
                device.setOnline(1);
                tunnelDevicelistService.updateById(device);
                log.info("[设备状态更新] 设备ID: {}, 名称: {}, 状态已更新为离线 - 原因: {}", 
                        device.getDeviceId(), deviceName, reason);
            }

            // 发送通知给管理员
            if (adminUsers != null && !adminUsers.isEmpty()) {
                addOfflineNotification(device.getDeviceId(), deviceName, "边缘控制器", adminUsers);
            }
        } catch (Exception e) {
            log.error("[设备状态更新] 更新设备状态失败 - 设备ID: {}, 名称: {}", 
                    device.getDeviceId(), deviceName, e);
        }
    }

    /**
     * 获取管理员用户列表（带缓存）
     */
    private List<SysUser> getAdminUsersWithCache() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (cachedAdminUsers != null && (currentTime - cacheTimestamp) < CACHE_EXPIRE_TIME) {
            return cachedAdminUsers;
        }
        
        // 缓存失效，重新查询
        List<SysUser> adminUsers = getAdminUsers();
        
        // 更新缓存
        if (adminUsers != null) {
            cachedAdminUsers = new ArrayList<>(adminUsers);
            cacheTimestamp = currentTime;
        } else {
            cachedAdminUsers = null;
            cacheTimestamp = 0;
        }
        
        return adminUsers;
    }

    /**
     * 获取管理员用户列表
     */
    private List<SysUser> getAdminUsers() {
        try {
            List<String> adminEmails = adminConfig.getAdminEmails();
            List<String> adminPhones = adminConfig.getAdminPhones();
            
            if (adminEmails == null || adminEmails.isEmpty()) {
                log.warn("[管理员配置] 未配置管理员邮箱");
                return new ArrayList<>();
            }
            
            List<SysUser> adminUsers = new ArrayList<>();
            
            for (int i = 0; i < adminEmails.size(); i++) {
                String email = adminEmails.get(i);
                String phone = (adminPhones != null && i < adminPhones.size()) ? adminPhones.get(i) : null;
                
                SysUser adminUser = new SysUser();
                adminUser.setUserId((long) (i + 1));
                adminUser.setUserName("admin_" + (i + 1));
                adminUser.setEmail(email);
                adminUser.setPhonenumber(phone);
                adminUser.setStatus("0");
                adminUser.setDelFlag("0");
                
                adminUsers.add(adminUser);
            }
            
            return adminUsers;
        } catch (Exception e) {
            log.error("[管理员配置] 读取管理员信息失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 添加掉线通知到队列
     * 注意：调用此方法前，应该先调用 shouldSendNotification() 检查是否应该发送
     */
    private void addOfflineNotification(Long deviceId, String deviceName, String deviceType, 
                                        List<SysUser> adminUsers) {
        int emailCount = 0;
        int skipEmailCount = 0;
        
        for (SysUser admin : adminUsers) {
            if (admin.getEmail() != null && !admin.getEmail().trim().isEmpty()) {
                // ========== 双重防重复检查 ==========
                // 虽然调用前已经检查过，但这里再次检查，确保不会重复添加
                boolean hasPending = notificationQueueService.hasPendingNotification(
                        deviceId, 
                        TunnelNotificationQueue.NotificationType.EMAIL,
                        admin.getEmail(),
                        admin.getUserId());
                boolean hasRecent = notificationQueueService.hasRecentNotification(
                        deviceId,
                        TunnelNotificationQueue.NotificationType.EMAIL,
                        admin.getEmail(),
                        admin.getUserId(),
                        duplicateCheckHours);
                
                if (hasPending || hasRecent) {
                    skipEmailCount++;
                    log.debug("[添加通知] 设备ID: {} 已有待发送或最近已发送的通知，跳过 - 收件人: {}", 
                            deviceId, admin.getEmail());
                    continue;
                }
                
                String emailContent = buildNotificationContent(deviceName, deviceId, deviceType);
                boolean success = notificationQueueService.addNotificationToQueue(
                        deviceId,
                        deviceName,
                        deviceType,
                        TunnelNotificationQueue.NotificationType.EMAIL,
                        admin.getEmail(),
                        null,
                        admin.getUserId(),
                        emailContent
                );
                
                if (success) {
                    emailCount++;
                    log.info("[添加通知] ✓ 邮件通知已添加到队列 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                            deviceId, deviceType, admin.getEmail());
                } else {
                    log.error("[添加通知] ✗ 邮件通知添加失败 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                            deviceId, deviceType, admin.getEmail());
                }
            }
        }
        
        log.info("[添加通知] 通知添加完成 - 设备ID: {}, 设备类型: {}, 邮件: {} 条（跳过: {} 条）", 
                deviceId, deviceType, emailCount, skipEmailCount);
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(String deviceName, Long deviceId, String deviceType) {
        return String.format("设备掉线通知：设备名称：%s，设备ID：%s，设备类型：%s", 
                deviceName, deviceId, deviceType);
    }
}

