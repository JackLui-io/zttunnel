package com.scsdky.web.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.core.domain.entity.SysRole;
import com.scsdky.common.core.domain.entity.SysUser;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelNotificationQueue;
import com.scsdky.web.config.AdminConfig;
import com.scsdky.web.service.TunnelApproachLampsTerminalService;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelNotificationQueueService;
import com.scsdky.web.utils.ConvertBit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 设备掉线检查定时任务
 * 每1分钟检查一次设备掉线情况，发现掉线后添加到通知队列
 * 
 * 检查的设备类型：
 * 1. 边缘控制器、电能终端 (tunnel_devicelist) - Online字段：0=在线，1=离线
 * 2. 灯具控制器 (tunnel_lamps_terminal) - lampsStatus字段bit0：0=正常，1=异常（掉线）
 * 3. 引道灯控制器 (tunnel_approach_lamps_terminal) - status字段：0=离线，1=在线
 * 
 * @author system
 */
@Slf4j
@Component
public class DeviceOfflineCheckTask {

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    private TunnelApproachLampsTerminalService tunnelApproachLampsTerminalService;

    @Resource
    private TunnelNotificationQueueService notificationQueueService;

    @Autowired
    private AdminConfig adminConfig;

    @Value("${notification.duplicate-check-hours:30}")
    private Integer duplicateCheckHours;  // 防重复通知时间（小时），默认30小时

    @Value("${notification.check-offline-days:3}")
    private Integer checkOfflineDays;  // 只检查最近N天内掉线的设备，默认3天

    // 管理员用户缓存
    private List<SysUser> cachedAdminUsers = null;
    // 缓存时间戳（毫秒）
    private AtomicLong cacheTimestamp = new AtomicLong(0);
    // 缓存有效期（毫秒），默认5分钟
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000;

    /**
     * 【已关闭】每1分钟检查一次设备掉线情况
     * cron表达式：0 0/1 * * * ? 表示每分钟的第0秒执行
     * 
     * 注意：此功能已迁移到独立进程健康巡检服务，此处已关闭
     */
    // @Scheduled(cron = "0 0/1 * * * ?")
    // public void checkDeviceOffline() {
    //     executeCheckDeviceOffline();
    // }

    /**
     * 执行设备掉线检查（可被启动时立即调用）
     */
    public void executeCheckDeviceOffline() {
        log.info("========== 开始执行设备掉线检查任务 ==========");
        
        try {
            // 查询所有管理员用户（使用缓存）
            log.info("[步骤1] 查询管理员用户...");
            List<SysUser> adminUsers = getAdminUsersWithCache();
            if (adminUsers == null || adminUsers.isEmpty()) {
                log.warn("[步骤1] 未找到管理员用户，无法发送通知，任务结束");
                return;
            }
            log.info("[步骤1] 查询完成，共找到 {} 个管理员用户", adminUsers.size());
            
            // 打印管理员用户信息
            log.info("[管理员列表] 管理员详情：");
            for (SysUser admin : adminUsers) {
                log.info("  - 用户ID: {}, 用户名: {}, 邮箱: {}, 手机号: {}", 
                        admin.getUserId(),
                        admin.getUserName(),
                        admin.getEmail() != null ? admin.getEmail() : "未配置",
                        admin.getPhonenumber() != null ? admin.getPhonenumber() : "未配置");
            }

            int totalOfflineCount = 0;

            // ========== 检查1：边缘控制器和电能终端 ==========
            log.info("[步骤2] ========== 检查边缘控制器和电能终端 ==========");
            int devicelistOfflineCount = checkDevicelistOffline(adminUsers);
            totalOfflineCount += devicelistOfflineCount;

            // ========== 检查2：灯具控制器 ==========
            log.info("[步骤3] ========== 检查灯具控制器 ==========");
            int lampsOfflineCount = checkLampsTerminalOffline(adminUsers);
            totalOfflineCount += lampsOfflineCount;

            // ========== 检查3：引道灯控制器 ==========
            log.info("[步骤4] ========== 检查引道灯控制器 ==========");
            int approachLampsOfflineCount = checkApproachLampsTerminalOffline(adminUsers);
            totalOfflineCount += approachLampsOfflineCount;

            log.info("========== 设备掉线检查任务完成，共发现 {} 个掉线设备（边缘/电能: {}, 灯具: {}, 引道灯: {}）==========", 
                    totalOfflineCount, devicelistOfflineCount, lampsOfflineCount, approachLampsOfflineCount);
        } catch (Exception e) {
            log.error("========== 设备掉线检查任务执行失败 ==========", e);
        }
    }

    /**
     * 检查边缘控制器和电能终端掉线情况
     * 表：tunnel_devicelist
     * 字段：Online (0=在线, 1=离线)
     * 设备类型：deviceTypeId=1(边缘控制器), deviceTypeId=2(电能终端)
     */
    private int checkDevicelistOffline(List<SysUser> adminUsers) {
        int offlineCount = 0;
        int onlineCount = 0;
        int unknownCount = 0;

        try {
            // 优化：只查询掉线设备（Online=1），且最近N天内有更新的设备
            // 计算时间阈值：当前时间往前推N天
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -checkOfflineDays);
            Date timeThreshold = cal.getTime();
            
            log.info("[边缘/电能] 查询掉线的边缘控制器和电能终端设备（最近{}天内）...", checkOfflineDays);
            List<TunnelDevicelist> edgeDevices = tunnelDevicelistService.list(
                    new LambdaQueryWrapper<TunnelDevicelist>()
                            .eq(TunnelDevicelist::getDeviceTypeId, 1)
                            .eq(TunnelDevicelist::getOnline, 1)  // 只查询掉线设备（Online=1）
                            .ge(TunnelDevicelist::getLastUpdate, timeThreshold)  // 最近N天内有更新
            );
            List<TunnelDevicelist> powerDevices = tunnelDevicelistService.list(
                    new LambdaQueryWrapper<TunnelDevicelist>()
                            .eq(TunnelDevicelist::getDeviceTypeId, 2)
                            .eq(TunnelDevicelist::getOnline, 1)  // 只查询掉线设备（Online=1）
                            .ge(TunnelDevicelist::getLastUpdate, timeThreshold)  // 最近N天内有更新
            );

            List<TunnelDevicelist> allDevices = new ArrayList<>();
            if (edgeDevices != null) allDevices.addAll(edgeDevices);
            if (powerDevices != null) allDevices.addAll(powerDevices);

            if (allDevices.isEmpty()) {
                log.warn("[边缘/电能] 未找到边缘控制器或电能终端设备");
                return 0;
            }

            log.info("[边缘/电能] 查询完成 - 边缘控制器: {} 个, 电能终端: {} 个, 总计: {} 个", 
                    edgeDevices != null ? edgeDevices.size() : 0,
                    powerDevices != null ? powerDevices.size() : 0,
                    allDevices.size());

            // 检查每个设备的在线状态
            for (TunnelDevicelist device : allDevices) {
                Integer onlineStatus = device.getOnline();
                String deviceName = device.getNickName() != null ? device.getNickName() : "设备" + device.getDeviceId();
                String deviceType = device.getDeviceTypeId() == 1 ? "边缘控制器" : "电能终端";
                
                if (onlineStatus == null) {
                    log.warn("[边缘/电能] 设备ID: {}, 名称: {}, 类型: {}, 状态: 未知（Online字段为null）", 
                            device.getDeviceId(), deviceName, deviceType);
                    unknownCount++;
                } else if (onlineStatus == 0) {
                    log.debug("[边缘/电能] 设备ID: {}, 名称: {}, 类型: {}, 状态: 在线", 
                            device.getDeviceId(), deviceName, deviceType);
                    onlineCount++;
                } else if (onlineStatus == 1) {
                    log.warn("[边缘/电能] 设备ID: {}, 名称: {}, 类型: {}, 状态: 离线（需要发送通知）", 
                            device.getDeviceId(), deviceName, deviceType);
                    // 设备已掉线，添加到通知队列
                    addOfflineNotification(device.getDeviceId(), deviceName, deviceType, adminUsers);
                    offlineCount++;
                } else {
                    log.warn("[边缘/电能] 设备ID: {}, 名称: {}, 类型: {}, 状态: 异常值（{}）", 
                            device.getDeviceId(), deviceName, deviceType, onlineStatus);
                    unknownCount++;
                }
            }

            log.info("[边缘/电能] 检查完成 - 在线: {} 个, 离线: {} 个, 未知: {} 个", 
                    onlineCount, offlineCount, unknownCount);
        } catch (Exception e) {
            log.error("[边缘/电能] 检查边缘控制器和电能终端掉线失败", e);
        }

        return offlineCount;
    }

    /**
     * 检查灯具控制器掉线情况
     * 表：tunnel_lamps_terminal
     * 字段1：deviceStatus (String) - "00"=正常，其他值=异常/掉线
     * 字段2：lampsStatus (Integer) - 0=正常，其他值（非0）=异常/掉线
     * 
     * 判断逻辑：只要满足以下任一条件，就认为设备掉线：
     * 1. deviceStatus != "00"
     * 2. lampsStatus != 0 且 lampsStatus != null
     */
    private int checkLampsTerminalOffline(List<SysUser> adminUsers) {
        int offlineCount = 0;
        int onlineCount = 0;
        int unknownCount = 0;

        try {
            // 优化：只查询掉线设备，且最近N天内有更新的设备
            // 计算时间阈值：当前时间往前推N天
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -checkOfflineDays);
            Date timeThreshold = cal.getTime();
            
            // 掉线条件：deviceStatus != "00" 或 lampsStatus != 0
            // 注意：由于MyBatis-Plus的LambdaQueryWrapper对null值处理有限，这里先查询最近N天内有更新的，然后在代码中过滤掉线设备
            log.info("[灯具] 查询灯具控制器设备（最近{}天内，将过滤掉线设备）...", checkOfflineDays);
            List<TunnelLampsTerminal> allLamps = tunnelLampsTerminalService.list(
                    new LambdaQueryWrapper<TunnelLampsTerminal>()
                            .ge(TunnelLampsTerminal::getUpdateTime, timeThreshold)  // 最近N天内有更新
            );
            
            // 过滤出掉线设备
            List<TunnelLampsTerminal> lamps = allLamps.stream()
                    .filter(lamp -> {
                        String deviceStatus = lamp.getDeviceStatus();
                        Integer lampsStatus = lamp.getLampsStatus();
                        // 掉线条件：deviceStatus != "00" 或 lampsStatus != 0
                        boolean isOffline = (deviceStatus != null && !"00".equals(deviceStatus)) 
                                || (lampsStatus != null && lampsStatus != 0);
                        return isOffline;
                    })
                    .collect(java.util.stream.Collectors.toList());

            if (lamps == null || lamps.isEmpty()) {
                log.warn("[灯具] 未找到灯具控制器设备");
                return 0;
            }

            log.info("[灯具] 查询完成，共找到 {} 个灯具控制器设备", lamps.size());

            // 检查每个灯具的在线状态
            for (TunnelLampsTerminal lamp : lamps) {
                String deviceStatus = lamp.getDeviceStatus();
                Integer lampsStatus = lamp.getLampsStatus();
                String deviceName = lamp.getLoopNumber() != null ? lamp.getLoopNumber() : 
                        (lamp.getDeviceName() != null ? lamp.getDeviceName() : "灯具" + lamp.getUniqueId());
                Long deviceId = lamp.getUniqueId() != null ? lamp.getUniqueId() : lamp.getDeviceId();

                // 判断是否掉线的标志
                boolean isOffline = false;
                List<String> offlineReasons = new ArrayList<>();

                // 1. 检查 deviceStatus 字段："00"=正常，其他值=异常
                if (deviceStatus == null || deviceStatus.trim().isEmpty()) {
                    // deviceStatus 为 null 或空，如果 lampsStatus 也为 null 或 0，则认为是未知状态
                    if (lampsStatus == null || lampsStatus == 0) {
                        log.warn("[灯具] 设备ID: {}, 名称: {}, 状态: 未知（deviceStatus和lampsStatus都为空或正常）", 
                                deviceId, deviceName);
                        unknownCount++;
                        continue;
                    } else {
                        // lampsStatus 不为0，认为异常
                        isOffline = true;
                        offlineReasons.add("设备状态未知但lampsStatus异常(lampsStatus=" + lampsStatus + ")");
                    }
                } else if (!"00".equals(deviceStatus)) {
                    // deviceStatus != "00" 表示异常/掉线
                    isOffline = true;
                    offlineReasons.add("设备状态异常(deviceStatus=" + deviceStatus + ")");
                }

                // 2. 检查 lampsStatus 字段：0=正常，非0=异常
                if (lampsStatus != null && lampsStatus != 0) {
                    // lampsStatus != 0 表示异常/掉线
                    isOffline = true;
                    offlineReasons.add("灯具状态异常(lampsStatus=" + lampsStatus + ")");
                }

                // 根据判断结果处理
                if (isOffline) {
                    String reasonStr = String.join(", ", offlineReasons);
                    log.warn("[灯具] 设备ID: {}, 名称: {}, 状态: 离线 - 原因: {} (deviceStatus={}, lampsStatus={})", 
                            deviceId, deviceName, reasonStr, deviceStatus, lampsStatus);
                    addOfflineNotification(deviceId, deviceName, "灯具控制器", adminUsers);
                    offlineCount++;
                } else {
                    log.debug("[灯具] 设备ID: {}, 名称: {}, 状态: 在线 (deviceStatus={}, lampsStatus={})", 
                            deviceId, deviceName, deviceStatus, lampsStatus);
                    onlineCount++;
                }
            }

            log.info("[灯具] 检查完成 - 在线: {} 个, 离线: {} 个, 未知: {} 个", 
                    onlineCount, offlineCount, unknownCount);
        } catch (Exception e) {
            log.error("[灯具] 检查灯具控制器掉线失败", e);
        }

        return offlineCount;
    }

    /**
     * 检查引道灯控制器掉线情况
     * 表：tunnel_approach_lamps_terminal
     * 字段：status (0=离线, 1=在线)
     */
    private int checkApproachLampsTerminalOffline(List<SysUser> adminUsers) {
        int offlineCount = 0;
        int onlineCount = 0;
        int unknownCount = 0;

        try {
            // 优化：只查询掉线设备（status=0），且最近N天内有更新的设备
            // 计算时间阈值：当前时间往前推N天
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -checkOfflineDays);
            // 将Date转换为LocalDateTime
            java.time.LocalDateTime timeThreshold = java.time.LocalDateTime.ofInstant(
                    cal.toInstant(), 
                    java.time.ZoneId.systemDefault()
            );
            
            log.info("[引道灯] 查询掉线的引道灯控制器设备（最近{}天内）...", checkOfflineDays);
            List<TunnelApproachLampsTerminal> approachLamps = tunnelApproachLampsTerminalService.list(
                    new LambdaQueryWrapper<TunnelApproachLampsTerminal>()
                            .eq(TunnelApproachLampsTerminal::getStatus, 0)  // 只查询掉线设备（status=0）
                            .ge(TunnelApproachLampsTerminal::getLastUpdate, timeThreshold)  // 最近N天内有更新
            );

            if (approachLamps == null || approachLamps.isEmpty()) {
                log.warn("[引道灯] 未找到引道灯控制器设备");
                return 0;
            }

            log.info("[引道灯] 查询完成，共找到 {} 个引道灯控制器设备", approachLamps.size());

            // 检查每个引道灯的在线状态
            for (TunnelApproachLampsTerminal lamp : approachLamps) {
                Integer status = lamp.getStatus();
                String deviceName = "引道灯" + (lamp.getDeviceNo() != null ? lamp.getDeviceNo() : lamp.getId());
                Long deviceId = lamp.getId() != null ? lamp.getId() : 
                        (lamp.getDeviceNo() != null ? Long.valueOf(lamp.getDeviceNo()) : 0L);

                // status字段：0=离线，1=在线
                if (status == null) {
                    log.warn("[引道灯] 设备ID: {}, 名称: {}, 状态: 未知（status字段为null）", 
                            deviceId, deviceName);
                    unknownCount++;
                } else if (status == 0) {
                    log.warn("[引道灯] 设备ID: {}, 名称: {}, 状态: 离线（需要发送通知）", 
                            deviceId, deviceName);
                    addOfflineNotification(deviceId, deviceName, "引道灯控制器", adminUsers);
                    offlineCount++;
                } else if (status == 1) {
                    log.debug("[引道灯] 设备ID: {}, 名称: {}, 状态: 在线", 
                            deviceId, deviceName);
                    onlineCount++;
                } else {
                    log.warn("[引道灯] 设备ID: {}, 名称: {}, 状态: 异常值（{}）", 
                            deviceId, deviceName, status);
                    unknownCount++;
                }
            }

            log.info("[引道灯] 检查完成 - 在线: {} 个, 离线: {} 个, 未知: {} 个", 
                    onlineCount, offlineCount, unknownCount);
        } catch (Exception e) {
            log.error("[引道灯] 检查引道灯控制器掉线失败", e);
        }

        return offlineCount;
    }

    /**
     * 获取管理员用户列表（带缓存）
     * 缓存有效期5分钟，减少数据库查询频率
     * 
     * @return 管理员用户列表
     */
    private List<SysUser> getAdminUsersWithCache() {
        long currentTime = System.currentTimeMillis();
        long cacheTime = cacheTimestamp.get();
        
        // 检查缓存是否有效
        if (cachedAdminUsers != null && (currentTime - cacheTime) < CACHE_EXPIRE_TIME) {
            log.debug("[管理员缓存] 使用缓存的管理员列表，缓存时间: {} 秒前", 
                    (currentTime - cacheTime) / 1000);
            return cachedAdminUsers;
        }
        
        // 缓存失效或不存在，重新查询
        log.debug("[管理员缓存] 缓存失效，重新查询管理员列表");
        List<SysUser> adminUsers = getAdminUsers();
        
        // 更新缓存
        if (adminUsers != null) {
            cachedAdminUsers = new ArrayList<>(adminUsers);
            cacheTimestamp.set(currentTime);
            log.info("[管理员缓存] 管理员列表已更新到缓存，共 {} 个管理员", adminUsers.size());
        } else {
            cachedAdminUsers = null;
            cacheTimestamp.set(0);
        }
        
        return adminUsers;
    }

    /**
     * 获取管理员用户列表
     * 从配置文件（jar包同级目录的 admin-config.properties）读取管理员邮箱
     * 不再从数据库查询
     */
    private List<SysUser> getAdminUsers() {
        try {
            log.debug("[查询管理员] 从配置文件读取管理员邮箱...");
            
            // 从配置文件读取管理员邮箱
            List<String> adminEmails = adminConfig.getAdminEmails();
            List<String> adminPhones = adminConfig.getAdminPhones();
            
            if (adminEmails == null || adminEmails.isEmpty()) {
                log.warn("[查询管理员] 配置文件中未配置管理员邮箱");
                return new ArrayList<>();
            }
            
            // 构建管理员用户列表（虚拟用户对象，只包含邮箱和手机号）
            List<SysUser> adminUsers = new ArrayList<>();
            
            // 为每个邮箱创建一个虚拟用户对象
            for (int i = 0; i < adminEmails.size(); i++) {
                String email = adminEmails.get(i);
                String phone = (adminPhones != null && i < adminPhones.size()) ? adminPhones.get(i) : null;
                
                SysUser adminUser = new SysUser();
                adminUser.setUserId((long) (i + 1)); // 虚拟用户ID
                adminUser.setUserName("admin_" + (i + 1)); // 虚拟用户名
                adminUser.setEmail(email);
                adminUser.setPhonenumber(phone);
                adminUser.setStatus("0"); // 状态正常
                adminUser.setDelFlag("0"); // 未删除
                
                adminUsers.add(adminUser);
                log.debug("[查询管理员] 加载管理员: 邮箱={}, 手机号={}", email, phone != null ? phone : "未配置");
            }
            
            log.info("[查询管理员] 从配置文件加载完成，共 {} 个管理员", adminUsers.size());
            return adminUsers;
        } catch (Exception e) {
            log.error("[查询管理员] 从配置文件读取管理员信息失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 添加掉线通知到队列（通用方法，支持所有设备类型）
     */
    private void addOfflineNotification(Long deviceId, String deviceName, String deviceType, List<SysUser> adminUsers) {
        log.info("[添加通知] 开始为掉线设备添加通知 - 设备ID: {}, 设备名称: {}, 设备类型: {}", 
                deviceId, deviceName, deviceType);
        
        int emailCount = 0;
        int smsCount = 0;
        int skipEmailCount = 0;
        int skipSmsCount = 0;
        
        // 为每个管理员添加邮件和短信通知
        for (SysUser admin : adminUsers) {
            log.debug("[添加通知] 处理管理员: {} (ID: {})", admin.getUserName(), admin.getUserId());
            
            // 添加邮件通知
            if (admin.getEmail() != null && !admin.getEmail().trim().isEmpty()) {
                // 防重复机制：检查是否已有待发送的通知，或最近N小时内已成功发送过通知
                // 注意：需要检查 deviceId + notificationType + recipientEmail 的组合，确保每个邮箱都能收到通知
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
                
                if (hasPending) {
                    log.debug("[添加通知] 设备ID: {} 已有待发送的邮件通知，跳过 - 收件人: {}", 
                            deviceId, admin.getEmail());
                    skipEmailCount++;
                } else if (hasRecent) {
                    log.debug("[添加通知] 设备ID: {} 最近{}小时内已发送过邮件通知，跳过 - 收件人: {}", 
                            deviceId, duplicateCheckHours, admin.getEmail());
                    skipEmailCount++;
                } else {
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
                        log.info("[添加通知] ✓ 邮件通知已添加到队列 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                                deviceId, deviceType, admin.getEmail());
                        emailCount++;
                    } else {
                        log.error("[添加通知] ✗ 邮件通知添加失败 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                                deviceId, deviceType, admin.getEmail());
                    }
                }
            } else {
                log.debug("[添加通知] 管理员 {} 未配置邮箱，跳过邮件通知", admin.getUserName());
            }

            // 添加短信通知（已暂停，暂时不使用短信通知）
            // TODO: 如需启用短信通知，请取消下面的注释
            /*
            if (admin.getPhonenumber() != null && !admin.getPhonenumber().trim().isEmpty()) {
                // 防重复机制：检查是否已有待发送的通知，或最近N小时内已成功发送过通知
                // 注意：需要检查 deviceId + notificationType + recipientUserId 的组合
                boolean hasPending = notificationQueueService.hasPendingNotification(
                        deviceId, 
                        TunnelNotificationQueue.NotificationType.SMS,
                        null,
                        admin.getUserId());
                boolean hasRecent = notificationQueueService.hasRecentNotification(
                        deviceId,
                        TunnelNotificationQueue.NotificationType.SMS,
                        null,
                        admin.getUserId(),
                        duplicateCheckHours);
                
                if (hasPending) {
                    log.debug("[添加通知] 设备ID: {} 已有待发送的短信通知，跳过 - 收件人: {}", 
                            deviceId, admin.getPhonenumber());
                    skipSmsCount++;
                } else if (hasRecent) {
                    log.debug("[添加通知] 设备ID: {} 最近{}小时内已发送过短信通知，跳过 - 收件人: {}", 
                            deviceId, duplicateCheckHours, admin.getPhonenumber());
                    skipSmsCount++;
                } else {
                    String smsContent = buildNotificationContent(deviceName, deviceId, deviceType);
                    boolean success = notificationQueueService.addNotificationToQueue(
                            deviceId,
                            deviceName,
                            deviceType,
                            TunnelNotificationQueue.NotificationType.SMS,
                            null,
                            admin.getPhonenumber(),
                            admin.getUserId(),
                            smsContent
                    );
                    
                    if (success) {
                        log.info("[添加通知] ✓ 短信通知已添加到队列 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                                deviceId, deviceType, admin.getPhonenumber());
                        smsCount++;
                    } else {
                        log.error("[添加通知] ✗ 短信通知添加失败 - 设备ID: {}, 设备类型: {}, 收件人: {}", 
                                deviceId, deviceType, admin.getPhonenumber());
                    }
                }
            } else {
                log.debug("[添加通知] 管理员 {} 未配置手机号，跳过短信通知", admin.getUserName());
            }
            */
            log.debug("[添加通知] 短信通知服务已暂停，跳过短信通知 - 管理员: {}", admin.getUserName());
        }
        
        log.info("[添加通知] 通知添加完成 - 设备ID: {}, 设备类型: {}, 邮件: {} 条（跳过: {} 条）, 短信: {} 条（跳过: {} 条）", 
                deviceId, deviceType, emailCount, skipEmailCount, smsCount, skipSmsCount);
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(String deviceName, Long deviceId, String deviceType) {
        return String.format("设备掉线通知：设备名称：%s，设备ID：%s，设备类型：%s", 
                deviceName, deviceId, deviceType);
    }
}
