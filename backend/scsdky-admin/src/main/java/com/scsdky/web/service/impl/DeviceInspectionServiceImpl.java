package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.framework.manager.AsyncManager;
import com.scsdky.web.config.AdminConfig;
import com.scsdky.web.domain.TunnelDeviceAlarm;
import com.scsdky.web.domain.TunnelDeviceExpectedInterval;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelTriggerLampsData;
import com.scsdky.web.domain.dto.DeviceAlarmSummaryDto;
import com.scsdky.web.domain.dto.DeviceAlarmStatisticsDto;
import com.scsdky.web.domain.dto.DeviceInspectionDto;
import com.scsdky.web.domain.dto.DeviceInspectionResultDto;
import com.scsdky.web.mapper.TunnelDeviceAlarmMapper;
import com.scsdky.web.mapper.TunnelEdgeComputeDataMapper;
import com.scsdky.web.mapper.TunnelTriggerLampsDataMapper;
import com.scsdky.web.service.DeviceInspectionService;
import com.scsdky.web.service.EmailService;
import com.scsdky.web.domain.TunnelEmail;
import com.scsdky.web.service.TunnelDeviceAlarmService;
import com.scsdky.web.service.TunnelDeviceExpectedIntervalService;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelEdgeComputeDataService;
import com.scsdky.web.service.TunnelEmailService;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelTriggerLampsDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * 设备巡检服务实现类
 * 
 * @author system
 */
@Slf4j
@Service
public class DeviceInspectionServiceImpl implements DeviceInspectionService {
    
    @Resource
    private TunnelDevicelistService tunnelDevicelistService;
    
    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;
    
    @Resource
    private TunnelDeviceExpectedIntervalService tunnelDeviceExpectedIntervalService;
    
    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;
    
    @Resource
    private TunnelTriggerLampsDataService tunnelTriggerLampsDataService;
    
    @Resource
    private TunnelDeviceAlarmService tunnelDeviceAlarmService;
    
    @Resource
    private TunnelEdgeComputeDataMapper tunnelEdgeComputeDataMapper;
    
    @Resource
    private TunnelTriggerLampsDataMapper tunnelTriggerLampsDataMapper;
    
    @Resource
    private TunnelDeviceAlarmMapper tunnelDeviceAlarmMapper;
    
    @Resource
    private EmailService emailService;
    
    @Resource
    private AdminConfig adminConfig;
    
    @Resource
    private TunnelEmailService tunnelEmailService;
    
    @Override
    public void executeInspectionAsync() {
        log.info("========== 提交设备巡检任务（异步执行） ==========");
        
        // 使用异步管理器执行任务
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                try {
                    log.info("========== 开始执行设备巡检任务（后台执行） ==========");
                    executeInspectionInternal();
                    log.info("========== 设备巡检任务执行完成 ==========");
                } catch (Exception e) {
                    log.error("设备巡检任务执行失败", e);
                }
            }
        });
    }
    
    @Override
    @Deprecated
    public AjaxResult executeInspection() {
        log.info("========== 开始执行设备巡检（同步执行，已废弃） ==========");
        try {
            executeInspectionInternal();
            return AjaxResult.success("设备巡检执行成功");
        } catch (Exception e) {
            log.error("设备巡检执行失败", e);
            return AjaxResult.error("设备巡检执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 设备巡检内部实现（同步执行）
     * 不返回详细日志，只记录到服务器日志
     */
    private void executeInspectionInternal() {
        try {
            // 第一步：查询边缘计算终端设备ID和对应的expected_interval
            // 1. 从 tunnel_devicelist 表中取出 device_type_id=1 的边缘计算终端 device_id
            List<TunnelDevicelist> edgeComputeDevices = tunnelDevicelistService.list(
                    new LambdaQueryWrapper<TunnelDevicelist>()
                            .eq(TunnelDevicelist::getDeviceTypeId, 1)  // device_type_id=1 表示边缘计算终端
            );
            
            // 2. 查询 tunnel_check_expected_interval 中 type=0 的所有记录，构建Map（unique_id -> expected_interval）
            List<TunnelDeviceExpectedInterval> edgeComputeIntervals = tunnelDeviceExpectedIntervalService.list(
                    new LambdaQueryWrapper<TunnelDeviceExpectedInterval>()
                            .eq(TunnelDeviceExpectedInterval::getType, 0)
            );
            
            // 构建Map：device_id -> expected_interval
            final Map<Long, Integer> edgeComputeIntervalMap = edgeComputeIntervals.stream()
                    .collect(Collectors.toMap(
                            TunnelDeviceExpectedInterval::getUniqueId,
                            TunnelDeviceExpectedInterval::getExpectedInterval,
                            (existing, replacement) -> existing  // 如果有重复key，保留第一个
                    ));
            
            // 3. 构建边缘计算终端数组（包含device_id和expected_interval）
            List<DeviceInspectionDto> edgeComputeDeviceList = new ArrayList<>();
            if (edgeComputeDevices != null && !edgeComputeDevices.isEmpty()) {
                for (TunnelDevicelist device : edgeComputeDevices) {
                    Long deviceId = device.getDeviceId();
                    // 如果未配置期望上报间隔，默认设为300秒（5分钟，因为边缘计算终端通常每5分钟上报一次）
                    Integer expectedInterval = edgeComputeIntervalMap.getOrDefault(deviceId, 300);
                    
                    // 适配数据库中的旧值：如果读取到5秒（旧值），自动转换为300秒（5分钟）
                    if (expectedInterval != null && expectedInterval == 5) {
                        expectedInterval = 300;
                        log.debug("边缘计算终端 deviceId={} 的期望间隔从旧值5秒自动适配为300秒", deviceId);
                    }
                    
                    DeviceInspectionDto dto = new DeviceInspectionDto();
                    dto.setDeviceId(deviceId);
                    dto.setExpectedInterval(expectedInterval);
                    dto.setType(0);  // type=0 表示边缘计算终端
                    edgeComputeDeviceList.add(dto);
                }
            }
            
            int edgeComputeCustomConfigCount = (int) edgeComputeDeviceList.stream()
                    .filter(dto -> edgeComputeIntervalMap.containsKey(dto.getDeviceId()))
                    .count();
            log.info("边缘计算终端数量：{}，其中{}个设备有自定义期望上报间隔配置（未配置的使用默认值300秒）", 
                    edgeComputeDeviceList.size(), edgeComputeCustomConfigCount);
            
            // 第二步：查询灯具终端设备ID和对应的expected_interval
            // 1. 从 tunnel_lamps_terminal 表中取出灯具 unique_id
            List<TunnelLampsTerminal> lampsTerminals = tunnelLampsTerminalService.list();
            
            // 2. 查询 tunnel_check_expected_interval 中 type=1 的所有记录，构建Map（unique_id -> expected_interval）
            List<TunnelDeviceExpectedInterval> lampsIntervals = tunnelDeviceExpectedIntervalService.list(
                    new LambdaQueryWrapper<TunnelDeviceExpectedInterval>()
                            .eq(TunnelDeviceExpectedInterval::getType, 1)
            );
            
            // 构建Map：unique_id -> expected_interval
            final Map<Long, Integer> lampsIntervalMap = lampsIntervals.stream()
                    .collect(Collectors.toMap(
                            TunnelDeviceExpectedInterval::getUniqueId,
                            TunnelDeviceExpectedInterval::getExpectedInterval,
                            (existing, replacement) -> existing  // 如果有重复key，保留第一个
                    ));
            
            // 3. 构建灯具终端数组（包含unique_id和expected_interval）
            List<DeviceInspectionDto> lampsTerminalList = new ArrayList<>();
            if (lampsTerminals != null && !lampsTerminals.isEmpty()) {
                for (TunnelLampsTerminal terminal : lampsTerminals) {
                    Long uniqueId = terminal.getUniqueId();
                    // 如果未配置期望上报间隔，默认设为60秒（1分钟，因为灯具终端通常每分钟上报一次）
                    Integer expectedInterval = lampsIntervalMap.getOrDefault(uniqueId, 60);
                    
                    // 适配数据库中的旧值：如果读取到1秒（旧值），自动转换为60秒（1分钟）
                    if (expectedInterval != null && expectedInterval == 1) {
                        expectedInterval = 60;
                        log.debug("灯具终端 uniqueId={} 的期望间隔从旧值1秒自动适配为60秒", uniqueId);
                    }
                    
                    DeviceInspectionDto dto = new DeviceInspectionDto();
                    dto.setDeviceId(uniqueId);
                    dto.setExpectedInterval(expectedInterval);
                    dto.setType(1);  // type=1 表示灯具终端
                    lampsTerminalList.add(dto);
                }
            }
            
            int lampsCustomConfigCount = (int) lampsTerminalList.stream()
                    .filter(dto -> lampsIntervalMap.containsKey(dto.getDeviceId()))
                    .count();
            log.info("灯具终端数量：{}，其中{}个设备有自定义期望上报间隔配置（未配置的使用默认值60秒）", 
                    lampsTerminalList.size(), lampsCustomConfigCount);
            
            // 第二步：检查边缘计算终端健康状态
            Date currentTime = new Date();
            log.info("当前时间：{}（不限制查询时间范围，直接查询最新记录）", currentTime.toString());
            
            List<TunnelDeviceAlarm> alarmList = new ArrayList<>();
            int edgeComputeHealthyCount = 0;
            int edgeComputeOfflineCount = 0;
            
            // 批量查询边缘计算终端的最新记录（优化：从346次查询减少到1次）
            List<Long> edgeComputeDeviceIds = edgeComputeDeviceList.stream()
                    .map(DeviceInspectionDto::getDeviceId)
                    .collect(Collectors.toList());
            
            Map<Long, TunnelEdgeComputeData> edgeComputeLatestDataMap = null;
            if (!edgeComputeDeviceIds.isEmpty()) {
                long startTime = System.currentTimeMillis();
                List<TunnelEdgeComputeData> edgeComputeLatestDataList = tunnelEdgeComputeDataMapper.selectLatestByDeviceIds(edgeComputeDeviceIds);
                long queryTime = System.currentTimeMillis() - startTime;
                log.info("批量查询边缘计算终端最新记录：{}个设备，查询耗时：{}ms", edgeComputeDeviceIds.size(), queryTime);
                
                // 构建Map：deviceId -> latestData
                edgeComputeLatestDataMap = edgeComputeLatestDataList.stream()
                        .collect(Collectors.toMap(
                                TunnelEdgeComputeData::getDevicelistId,
                                data -> data,
                                (existing, replacement) -> existing  // 如果有重复key，保留第一个
                        ));
            }
            
            // 检查边缘计算终端
            for (DeviceInspectionDto dto : edgeComputeDeviceList) {
                Long deviceId = dto.getDeviceId();
                Integer expectedInterval = dto.getExpectedInterval();
                
                // 从批量查询结果中获取该设备的最新记录
                TunnelEdgeComputeData latestData = edgeComputeLatestDataMap != null 
                        ? edgeComputeLatestDataMap.get(deviceId) 
                        : null;
                
                int status;
                long timeDiffSeconds = 0;
                
                if (latestData != null && latestData.getUploadTime() != null) {
                    // 计算时间差（秒）
                    timeDiffSeconds = (currentTime.getTime() - latestData.getUploadTime().getTime()) / 1000;
                    status = calculateHealthStatus(timeDiffSeconds, expectedInterval);
                    
                    // 调试日志：针对特定设备输出详细信息
                    if (deviceId == 4939L) {
                        log.warn("边缘计算终端健康检查详情 - deviceId={}, 期望间隔={}秒, 时间差={}秒, 最新上报时间={}, 判断状态={}（1=按时上报，0=未按时上报）",
                                deviceId, expectedInterval, timeDiffSeconds, latestData.getUploadTime(), status);
                    }
                } else {
                    // 没有找到数据，视为未按时上报
                    status = 0;  // 未按时上报
                    timeDiffSeconds = -1;  // 表示没有数据
                    
                    // 调试日志：针对特定设备输出详细信息
                    if (deviceId == 4939L) {
                        log.warn("边缘计算终端健康检查详情 - deviceId={}, 期望间隔={}秒, 未找到最新上报数据，判断状态=0（未按时上报）",
                                deviceId, expectedInterval);
                    }
                }
                
                // 记录统计
                if (status == 1) {
                    edgeComputeHealthyCount++;
                } else {
                    edgeComputeOfflineCount++;
                }
                
                // 创建告警记录
                TunnelDeviceAlarm alarm = new TunnelDeviceAlarm();
                alarm.setTypeId(0);  // 0=边缘计算终端
                alarm.setDeviceId(deviceId);
                alarm.setStatus(status);  // 1=按时上报，0=未按时上报
                alarm.setUpdateTime(currentTime);
                alarmList.add(alarm);
            }
            
            log.info("边缘计算终端健康检查完成：按时上报{}个，未按时上报{}个", 
                    edgeComputeHealthyCount, edgeComputeOfflineCount);
            
            // 边缘计算终端处理完成，释放不再使用的Map内存（帮助GC及时回收）
            // 注意：edgeComputeIntervalMap 是 final 变量，不能置null，但会在方法结束后自动回收
            edgeComputeLatestDataMap = null;
            edgeComputeDeviceIds = null;
            
            // 第三步：检查灯具终端健康状态
            int lampsHealthyCount = 0;
            int lampsOfflineCount = 0;
            
            // 批量查询灯具终端的最新记录（优化：从301次查询减少到1次）
            List<Long> lampsTerminalIds = lampsTerminalList.stream()
                    .map(DeviceInspectionDto::getDeviceId)
                    .collect(Collectors.toList());
            
            Map<Long, TunnelTriggerLampsData> lampsLatestDataMap = null;
            if (!lampsTerminalIds.isEmpty()) {
                long startTime = System.currentTimeMillis();
                List<TunnelTriggerLampsData> lampsLatestDataList = tunnelTriggerLampsDataMapper.selectLatestByUniqueIds(lampsTerminalIds);
                long queryTime = System.currentTimeMillis() - startTime;
                log.info("批量查询灯具终端最新记录：{}个设备，查询耗时：{}ms", lampsTerminalIds.size(), queryTime);
                
                // 构建Map：uniqueId -> latestData
                lampsLatestDataMap = lampsLatestDataList.stream()
                        .collect(Collectors.toMap(
                                TunnelTriggerLampsData::getUniqueId,
                                data -> data,
                                (existing, replacement) -> existing  // 如果有重复key，保留第一个
                        ));
            }
            
            // 检查灯具终端
            for (DeviceInspectionDto dto : lampsTerminalList) {
                Long uniqueId = dto.getDeviceId();
                Integer expectedInterval = dto.getExpectedInterval();
                
                // 从批量查询结果中获取该设备的最新记录
                TunnelTriggerLampsData latestData = lampsLatestDataMap != null 
                        ? lampsLatestDataMap.get(uniqueId) 
                        : null;
                
                int status;
                long timeDiffSeconds = 0;
                
                if (latestData != null && latestData.getUploadTime() != null) {
                    // 计算时间差（秒）
                    timeDiffSeconds = (currentTime.getTime() - latestData.getUploadTime().getTime()) / 1000;
                    status = calculateHealthStatus(timeDiffSeconds, expectedInterval);
                    
                    // 调试日志：针对特定设备输出详细信息
                    if (uniqueId == 4939L) {
                        log.warn("设备健康检查详情 - uniqueId={}, 期望间隔={}秒, 时间差={}秒, 最新上报时间={}, 判断状态={}（1=按时上报，0=未按时上报）",
                                uniqueId, expectedInterval, timeDiffSeconds, latestData.getUploadTime(), status);
                    }
                } else {
                    // 没有找到数据，视为未按时上报
                    status = 0;  // 未按时上报
                    timeDiffSeconds = -1;  // 表示没有数据
                    
                    // 调试日志：针对特定设备输出详细信息
                    if (uniqueId == 4939L) {
                        log.warn("设备健康检查详情 - uniqueId={}, 期望间隔={}秒, 未找到最新上报数据，判断状态=0（未按时上报）",
                                uniqueId, expectedInterval);
                    }
                }
                
                // 记录统计
                if (status == 1) {
                    lampsHealthyCount++;
                } else {
                    lampsOfflineCount++;
                }
                
                // 创建告警记录
                TunnelDeviceAlarm alarm = new TunnelDeviceAlarm();
                alarm.setTypeId(1);  // 1=灯具设备
                alarm.setDeviceId(uniqueId);
                alarm.setStatus(status);  // 1=按时上报，0=未按时上报
                alarm.setUpdateTime(currentTime);
                alarmList.add(alarm);
            }
            
            log.info("灯具终端健康检查完成：按时上报{}个，未按时上报{}个", 
                    lampsHealthyCount, lampsOfflineCount);
            
            // 灯具终端处理完成，释放不再使用的Map内存（帮助GC及时回收）
            // 注意：lampsIntervalMap 是 final 变量，不能置null，但会在方法结束后自动回收
            lampsLatestDataMap = null;
            lampsTerminalIds = null;
            
            // 第四步：批量写入告警记录
            if (!alarmList.isEmpty()) {
                log.info("准备写入告警记录：{}条", alarmList.size());
                
                try {
                    boolean saveResult = tunnelDeviceAlarmService.saveBatch(alarmList);
                    log.info("批量保存结果：{}（准备保存{}条）", saveResult, alarmList.size());
                    
                    // 验证是否真的保存成功
                    if (saveResult) {
                        long savedCount = tunnelDeviceAlarmService.count(
                                new LambdaQueryWrapper<TunnelDeviceAlarm>()
                                        .ge(TunnelDeviceAlarm::getUpdateTime, currentTime)
                        );
                        log.info("验证：数据库中实际保存的告警记录数量：{}（查询条件：updateTime >= {}）", 
                                savedCount, currentTime.toString());
                        
                        if (savedCount == 0) {
                            log.warn("警告：批量保存返回true，但数据库中查询不到记录！");
                        }
                    } else {
                        log.warn("批量保存返回false，保存失败！");
                    }
                } catch (Exception e) {
                    log.error("保存告警记录时发生异常", e);
                    
                    // 尝试逐条保存，以便定位问题
                    int successCount = 0;
                    int failCount = 0;
                    for (TunnelDeviceAlarm alarm : alarmList) {
                        try {
                            boolean result = tunnelDeviceAlarmService.save(alarm);
                            if (result) {
                                successCount++;
                            } else {
                                failCount++;
                                log.warn("保存告警记录失败：typeId={}, deviceId={}, status={}", 
                                        alarm.getTypeId(), alarm.getDeviceId(), alarm.getStatus());
                            }
                        } catch (Exception ex) {
                            failCount++;
                            log.error("保存单条告警记录异常：typeId={}, deviceId={}, status={}", 
                                    alarm.getTypeId(), alarm.getDeviceId(), alarm.getStatus(), ex);
                        }
                    }
                    log.info("逐条保存结果：成功{}条，失败{}条", successCount, failCount);
                }
            } else {
                log.info("没有告警记录需要写入（alarmList为空）");
            }
            
            int totalHealthy = edgeComputeHealthyCount + lampsHealthyCount;
            int totalOffline = edgeComputeOfflineCount + lampsOfflineCount;
            
            log.info("========== 巡检结果汇总 ==========");
            log.info("边缘计算终端：{}个（按时上报{}，未按时上报{}）", 
                    edgeComputeDeviceList.size(), edgeComputeHealthyCount, edgeComputeOfflineCount);
            log.info("灯具终端：{}个（按时上报{}，未按时上报{}）", 
                    lampsTerminalList.size(), lampsHealthyCount, lampsOfflineCount);
            log.info("总计：按时上报{}，未按时上报{}，告警记录列表大小：{}条", 
                    totalHealthy, totalOffline, alarmList.size());
        } catch (Exception e) {
            log.error("设备巡检执行失败", e);
            throw new RuntimeException("设备巡检执行失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public AjaxResult sendEmail() {
        log.info("========== 开始生成并发送每日健康巡检报告邮件（列出所有健康检查记录） ==========");
        
        try {
            // 第一步：查询所有设备的健康检查记录（不限制次数）
            long startTime = System.currentTimeMillis();
            List<TunnelDeviceAlarm> allAlarms = tunnelDeviceAlarmMapper.selectRecentAlarmsByDevice(0);  // 传入0表示查询所有记录
            long queryTime = System.currentTimeMillis() - startTime;
            log.info("查询到所有健康检查记录：{}条，耗时{}ms", allAlarms.size(), queryTime);
            
            // 按设备分组：key = typeId_deviceId，value = 该设备的所有记录列表（按id倒序）
            Map<String, List<TunnelDeviceAlarm>> deviceAlarmsMap = new HashMap<>();
            for (TunnelDeviceAlarm alarm : allAlarms) {
                String key = alarm.getTypeId() + "_" + alarm.getDeviceId();
                deviceAlarmsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(alarm);
            }
            
            // 对每个设备的记录列表，按id倒序排序（最新的在前面）
            for (Map.Entry<String, List<TunnelDeviceAlarm>> entry : deviceAlarmsMap.entrySet()) {
                List<TunnelDeviceAlarm> alarms = entry.getValue();
                // 按id倒序排序
                alarms.sort((a, b) -> b.getId().compareTo(a.getId()));
            }
            
            log.info("共有{}个设备有健康检查记录", deviceAlarmsMap.size());
            
            // 第二步：收集所有设备ID，用于查询设备信息
            Set<Long> allEdgeComputeIds = new HashSet<>();
            Set<Long> allLampsIds = new HashSet<>();
            
            for (Map.Entry<String, List<TunnelDeviceAlarm>> entry : deviceAlarmsMap.entrySet()) {
                String key = entry.getKey();
                String[] parts = key.split("_");
                Integer typeId = Integer.parseInt(parts[0]);
                Long deviceId = Long.parseLong(parts[1]);
                
                if (typeId == 0) {
                    allEdgeComputeIds.add(deviceId);
                } else if (typeId == 1) {
                    allLampsIds.add(deviceId);
                }
            }
            
            // 批量查询设备信息
            Map<Long, TunnelDevicelist> edgeComputeDeviceMap = batchQueryEdgeComputeDevices(new ArrayList<>(allEdgeComputeIds));
            Map<Long, TunnelLampsTerminal> lampsDeviceMap = batchQueryLampsDevices(new ArrayList<>(allLampsIds));
            
            // 第三步：构建设备巡检结果列表（包含每个设备的所有巡检结果）
            List<DeviceInspectionResultDto> deviceResults = new ArrayList<>();
            
            for (Map.Entry<String, List<TunnelDeviceAlarm>> entry : deviceAlarmsMap.entrySet()) {
                String key = entry.getKey();
                List<TunnelDeviceAlarm> alarms = entry.getValue();
                
                if (alarms.isEmpty()) {
                    continue;
                }
                
                // 解析设备信息
                String[] parts = key.split("_");
                Integer typeId = Integer.parseInt(parts[0]);
                Long deviceId = Long.parseLong(parts[1]);
                
                // 构建所有巡检结果字符串（从最新到最旧）
                StringBuilder statusString = new StringBuilder();
                for (TunnelDeviceAlarm alarm : alarms) {
                    statusString.append(alarm.getStatus());  // 1=按时上报，0=未按时上报
                }
                
                // 创建设备巡检结果DTO
                DeviceInspectionResultDto resultDto = new DeviceInspectionResultDto();
                resultDto.setTypeId(typeId);
                resultDto.setDeviceId(deviceId);
                resultDto.setStatusString(statusString.toString());
                
                // 填充设备显示名称
                if (typeId == 0) {
                    // 边缘计算终端
                    TunnelDevicelist device = edgeComputeDeviceMap.get(deviceId);
                    if (device != null) {
                        resultDto.setDisplayName(String.valueOf(deviceId) + " 边缘控制器");
                    } else {
                        resultDto.setDisplayName(String.valueOf(deviceId) + " 边缘控制器");
                    }
                } else if (typeId == 1) {
                    // 灯具终端
                    TunnelLampsTerminal terminal = lampsDeviceMap.get(deviceId);
                    if (terminal != null) {
                        // 格式：4785 灯具控制器 deviceID=1051,回路编号为R9-1
                        String displayName = deviceId.toString();  // uniqueId
                        displayName += " 灯具控制器";
                        if (terminal.getDeviceId() != null) {
                            displayName += " deviceID=" + terminal.getDeviceId();
                        }
                        if (terminal.getLoopNumber() != null && !terminal.getLoopNumber().isEmpty()) {
                            displayName += ",回路编号为" + terminal.getLoopNumber();
                        }
                        resultDto.setDisplayName(displayName);
                    } else {
                        resultDto.setDisplayName(deviceId + " 灯具控制器");
                    }
                }
                
                deviceResults.add(resultDto);
            }
            
            log.info("共收集到{}个设备的巡检结果", deviceResults.size());
            
            // 第四步：生成邮件内容
            String emailContent = buildDailyHealthReportEmail(deviceResults);
            
            // 第六步：发送邮件
            List<String> adminEmails = adminConfig.getAdminEmails();
            if (adminEmails == null || adminEmails.isEmpty()) {
                log.warn("未配置管理员邮箱，无法发送邮件");
                return AjaxResult.error("未配置管理员邮箱，无法发送邮件");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            // 邮件标题日期使用前一天（因为计划每天凌晨1:00发送）
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String subject = sdf.format(cal.getTime()) + " 日常巡检通知";
            
            int successCount = 0;
            int failCount = 0;
            Date sendTime = new Date();  // 记录发送时间
            
            for (String email : adminEmails) {
                try {
                    boolean result = emailService.sendEmail(email, subject, emailContent);
                    if (result) {
                        successCount++;
                        log.info("邮件发送成功：{}", email);
                        
                        // 记录邮件发送成功到数据库
                        try {
                            TunnelEmail emailRecord = new TunnelEmail();
                            emailRecord.setEmail(email);
                            emailRecord.setUpdateTime(sendTime);
                            boolean saveResult = tunnelEmailService.save(emailRecord);
                            if (saveResult) {
                                log.debug("邮件发送记录已保存到数据库：{}", email);
                            } else {
                                log.warn("邮件发送记录保存失败：{}", email);
                            }
                        } catch (Exception e) {
                            // 记录失败不影响主流程，只记录日志
                            log.error("保存邮件发送记录异常：{}", email, e);
                        }
                    } else {
                        failCount++;
                        log.warn("邮件发送失败：{}", email);
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("发送邮件异常：{}", email, e);
                }
            }
            
            log.info("========== 每日健康巡检报告邮件发送完成：成功{}封，失败{}封 ==========", successCount, failCount);
            
            // 第七步：清空 tunnel_check_alarm 表
            try {
                tunnelDeviceAlarmMapper.truncateTable();
                log.info("========== tunnel_check_alarm 表已清空 ==========");
            } catch (Exception e) {
                log.error("清空 tunnel_check_alarm 表失败", e);
                // 清空表失败不影响返回结果，只记录日志
            }
            
            return AjaxResult.success(String.format("邮件发送完成：成功%d封，失败%d封", successCount, failCount));
        } catch (Exception e) {
            log.error("发送每日健康巡检报告邮件失败", e);
            return AjaxResult.error("发送邮件失败：" + e.getMessage());
        }
    }
    
    /**
     * 填充设备信息（名称、显示名称、未上报时长等）
     */
    private void fillDeviceInfo(DeviceAlarmSummaryDto dto, 
                                Map<Long, TunnelDevicelist> edgeComputeDeviceMap,
                                Map<Long, TunnelLampsTerminal> lampsDeviceMap,
                                Map<Long, Date> edgeComputeLatestUploadTimeMap,
                                Map<Long, Date> lampsLatestUploadTimeMap,
                                Date currentTime) {
        if (dto.getTypeId() == 0) {
            // 边缘计算终端
            TunnelDevicelist device = edgeComputeDeviceMap.get(dto.getDeviceId());
            if (device != null) {
                String deviceName = device.getNickName() != null ? device.getNickName() : "边缘控制器";
                dto.setDeviceName(deviceName);
                dto.setDisplayName(dto.getDeviceId() + " " + deviceName);
            } else {
                dto.setDeviceName("边缘控制器");
                dto.setDisplayName(dto.getDeviceId() + " 边缘控制器");
            }
            
            // 计算未上报时长
            if (edgeComputeLatestUploadTimeMap != null) {
                Date latestUploadTime = edgeComputeLatestUploadTimeMap.get(dto.getDeviceId());
                if (latestUploadTime != null) {
                    long hoursDiff = (currentTime.getTime() - latestUploadTime.getTime()) / (1000 * 60 * 60);
                    dto.setHoursSinceLastReport(hoursDiff);
                }
            }
        } else {
            // 灯具终端
            TunnelLampsTerminal terminal = lampsDeviceMap.get(dto.getDeviceId());
            if (terminal != null) {
                String deviceName = terminal.getDeviceName() != null ? terminal.getDeviceName() : "灯具控制器";
                String loopNumber = terminal.getLoopNumber() != null ? terminal.getLoopNumber() : "";
                String deviceIdStr = terminal.getDeviceId() != null ? String.valueOf(terminal.getDeviceId()) : "";
                
                dto.setDeviceName(deviceName);
                
                // 构建显示名称：设备ID + 设备名称 + 回路编号
                StringBuilder displayName = new StringBuilder();
                displayName.append(dto.getDeviceId()).append(" ").append(deviceName);
                if (deviceIdStr.length() > 0) {
                    displayName.append(" deviceID=").append(deviceIdStr);
                }
                if (loopNumber.length() > 0) {
                    displayName.append(",回路编号为").append(loopNumber);
                }
                dto.setDisplayName(displayName.toString());
            } else {
                dto.setDeviceName("灯具控制器");
                dto.setDisplayName(dto.getDeviceId() + " 灯具控制器");
            }
            
            // 计算未上报时长
            if (lampsLatestUploadTimeMap != null) {
                Date latestUploadTime = lampsLatestUploadTimeMap.get(dto.getDeviceId());
                if (latestUploadTime != null) {
                    long hoursDiff = (currentTime.getTime() - latestUploadTime.getTime()) / (1000 * 60 * 60);
                    dto.setHoursSinceLastReport(hoursDiff);
                }
            }
        }
    }
    
    /**
     * 构建每日健康巡检报告邮件内容
     * 列出每个设备的所有巡检结果，按设备类型分组，并按状态字符串排序
     */
    private String buildDailyHealthReportEmail(List<DeviceInspectionResultDto> deviceResults) {
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy.MM.dd");
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.8; color: #333; }");
        html.append(".container { max-width: 800px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }");
        html.append(".content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; }");
        html.append(".device-group { margin-bottom: 20px; }");
        html.append(".group-title { font-weight: bold; font-size: 16px; margin-bottom: 10px; color: #2196F3; }");
        html.append(".device-item { margin: 8px 0; padding: 8px; background-color: white; border-left: 3px solid #2196F3; }");
        html.append(".footer { margin-top: 20px; padding: 20px; text-align: center; color: #999; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        // 邮件标题日期使用前一天（因为计划每天凌晨1:00发送）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        html.append("<h2>").append(dateSdf.format(cal.getTime())).append(" 日常巡检通知 -> 24小时巡检报告</h2>");
        html.append("</div>");
        html.append("<div class='content'>");
        
        if (deviceResults.isEmpty()) {
            html.append("<p style='color: #999;'>无设备巡检记录</p>");
        } else {
            // 统计各类设备数量
            int stableOnlineCount = 0;  // 稳定在线：全1的设备
            int fluctuatingOnlineCount = 0;  // 波动在线：混合的设备
            int offlineCount = 0;  // 掉线设备：全0的设备
            
            for (DeviceInspectionResultDto device : deviceResults) {
                int order = getStatusStringOrder(device.getStatusString());
                if (order == 0) {
                    stableOnlineCount++;  // 全是1
                } else if (order == 1) {
                    fluctuatingOnlineCount++;  // 1和0相间
                } else {
                    offlineCount++;  // 全是0
                }
            }
            
            // 在顶部显示统计信息
            html.append("<div style='background-color: #e3f2fd; padding: 15px; margin-bottom: 20px; border-radius: 5px;'>");
            html.append("<h3 style='margin-top: 0; color: #1976d2;'>设备状态统计</h3>");
            html.append("<div style='display: flex; justify-content: space-around; flex-wrap: wrap;'>");
            html.append("<div style='text-align: center; margin: 10px;'>");
            html.append("<div style='font-size: 24px; font-weight: bold; color: #4caf50;'>").append(stableOnlineCount).append("</div>");
            html.append("<div style='color: #666;'>稳定在线</div>");
            html.append("</div>");
            html.append("<div style='text-align: center; margin: 10px;'>");
            html.append("<div style='font-size: 24px; font-weight: bold; color: #ff9800;'>").append(fluctuatingOnlineCount).append("</div>");
            html.append("<div style='color: #666;'>波动在线</div>");
            html.append("</div>");
            html.append("<div style='text-align: center; margin: 10px;'>");
            html.append("<div style='font-size: 24px; font-weight: bold; color: #f44336;'>").append(offlineCount).append("</div>");
            html.append("<div style='color: #666;'>异常设备</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            
            // 按设备类型分组
            Map<Integer, List<DeviceInspectionResultDto>> groupedByType = deviceResults.stream()
                    .collect(Collectors.groupingBy(DeviceInspectionResultDto::getTypeId));
            
            // 按typeId排序（0=边缘计算终端，1=灯具设备）
            List<Integer> sortedTypes = groupedByType.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            for (Integer typeId : sortedTypes) {
                List<DeviceInspectionResultDto> devices = groupedByType.get(typeId);
                
                // 按状态字符串排序：全是1的在前，1和0相间的在中间，全是0的在后
                devices.sort((d1, d2) -> {
                    int order1 = getStatusStringOrder(d1.getStatusString());
                    int order2 = getStatusStringOrder(d2.getStatusString());
                    if (order1 != order2) {
                        return Integer.compare(order1, order2);
                    }
                    // 如果排序值相同，按deviceId排序
                    return Long.compare(d1.getDeviceId(), d2.getDeviceId());
                });
                
                // 输出分组标题
                String groupTitle = typeId == 0 ? "边缘计算终端" : "灯具设备";
                html.append("<div class='device-group'>");
                html.append("<div class='group-title'>").append(groupTitle).append("</div>");
                
                // 输出该分组下的所有设备
                for (DeviceInspectionResultDto device : devices) {
                    html.append("<div class='device-item'>");
                    html.append("- ").append(device.getDisplayName()).append("：").append(device.getStatusString());
                    html.append("</div>");
                }
                
                html.append("</div>");
            }
        }
        
        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>此邮件由隧道低碳照明智控平台自动发送，请勿回复。</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 获取状态字符串的排序值
     * 全是1的返回0（排在最前），1和0相间的返回1（排在中间），全是0的返回2（排在最后）
     * 
     * @param statusString 状态字符串（例如：1110011101）
     * @return 排序值
     */
    private int getStatusStringOrder(String statusString) {
        if (statusString == null || statusString.isEmpty()) {
            return 2; // 空字符串排在最后
        }
        
        // 检查是否全是1
        boolean allOnes = true;
        for (char c : statusString.toCharArray()) {
            if (c != '1') {
                allOnes = false;
                break;
            }
        }
        if (allOnes) {
            return 0; // 全是1，排在最前
        }
        
        // 检查是否全是0
        boolean allZeros = true;
        for (char c : statusString.toCharArray()) {
            if (c != '0') {
                allZeros = false;
                break;
            }
        }
        if (allZeros) {
            return 2; // 全是0，排在最后
        }
        
        // 1和0相间
        return 1; // 排在中间
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult refreshExpectedInterval() {
        log.info("========== 开始刷新期望上报间隔表（增量更新） ==========");
        
        try {
            int edgeComputeNewCount = 0;
            int lampsNewCount = 0;
            
            // 第一步：处理边缘计算终端（type=0）
            // 查询 tunnel_devicelist 中 device_type_id=1 的所有设备ID
            List<TunnelDevicelist> edgeComputeDevices = tunnelDevicelistService.list(
                    new LambdaQueryWrapper<TunnelDevicelist>()
                            .eq(TunnelDevicelist::getDeviceTypeId, 1)  // device_type_id=1 表示边缘计算终端
                            .select(TunnelDevicelist::getDeviceId)  // 只查询device_id字段，提高效率
            );
            
            if (edgeComputeDevices != null && !edgeComputeDevices.isEmpty()) {
                // 提取所有边缘计算终端的device_id
                List<Long> allEdgeComputeDeviceIds = edgeComputeDevices.stream()
                        .map(TunnelDevicelist::getDeviceId)
                        .collect(Collectors.toList());
                
                // 查询 tunnel_check_expected_interval 中已存在的 type=0 的设备ID
                List<TunnelDeviceExpectedInterval> existingEdgeComputeIntervals = tunnelDeviceExpectedIntervalService.list(
                        new LambdaQueryWrapper<TunnelDeviceExpectedInterval>()
                                .eq(TunnelDeviceExpectedInterval::getType, 0)
                                .select(TunnelDeviceExpectedInterval::getUniqueId)  // 只查询unique_id字段
                );
                
                // 提取已存在的设备ID
                List<Long> existingEdgeComputeDeviceIds = existingEdgeComputeIntervals.stream()
                        .map(TunnelDeviceExpectedInterval::getUniqueId)
                        .collect(Collectors.toList());
                
                // 找出新设备（在allEdgeComputeDeviceIds中但不在existingEdgeComputeDeviceIds中）
                List<Long> newEdgeComputeDeviceIds = allEdgeComputeDeviceIds.stream()
                        .filter(deviceId -> !existingEdgeComputeDeviceIds.contains(deviceId))
                        .collect(Collectors.toList());
                
                // 批量插入新设备
                if (!newEdgeComputeDeviceIds.isEmpty()) {
                    List<TunnelDeviceExpectedInterval> edgeComputeIntervals = new ArrayList<>();
                    Date now = new Date();
                    for (Long deviceId : newEdgeComputeDeviceIds) {
                        TunnelDeviceExpectedInterval interval = new TunnelDeviceExpectedInterval();
                        interval.setUniqueId(deviceId);
                        interval.setType(0);  // type=0 表示来自tunnel_devicelist
                        interval.setExpectedInterval(300);  // expected_interval=300秒（5分钟，因为边缘计算终端通常每5分钟上报一次）
                        interval.setUpdateTime(now);
                        edgeComputeIntervals.add(interval);
                    }
                    
                    boolean edgeSaveResult = tunnelDeviceExpectedIntervalService.saveBatch(edgeComputeIntervals);
                    edgeComputeNewCount = edgeComputeIntervals.size();
                    log.info("边缘计算终端：发现{}个新设备，插入结果：{}", edgeComputeNewCount, edgeSaveResult);
                } else {
                    log.info("边缘计算终端：没有新设备需要插入");
                }
            }
            
            // 第二步：处理灯具终端（type=1）
            // 查询 tunnel_lamps_terminal 中的所有灯具 unique_id
            List<TunnelLampsTerminal> lampsTerminals = tunnelLampsTerminalService.list(
                    new LambdaQueryWrapper<TunnelLampsTerminal>()
                            .select(TunnelLampsTerminal::getUniqueId)  // 只查询unique_id字段，提高效率
            );
            
            if (lampsTerminals != null && !lampsTerminals.isEmpty()) {
                // 提取所有灯具终端的unique_id
                List<Long> allLampsTerminalIds = lampsTerminals.stream()
                        .map(TunnelLampsTerminal::getUniqueId)
                        .collect(Collectors.toList());
                
                // 查询 tunnel_check_expected_interval 中已存在的 type=1 的设备ID
                List<TunnelDeviceExpectedInterval> existingLampsIntervals = tunnelDeviceExpectedIntervalService.list(
                        new LambdaQueryWrapper<TunnelDeviceExpectedInterval>()
                                .eq(TunnelDeviceExpectedInterval::getType, 1)
                                .select(TunnelDeviceExpectedInterval::getUniqueId)  // 只查询unique_id字段
                );
                
                // 提取已存在的设备ID
                List<Long> existingLampsTerminalIds = existingLampsIntervals.stream()
                        .map(TunnelDeviceExpectedInterval::getUniqueId)
                        .collect(Collectors.toList());
                
                // 找出新设备（在allLampsTerminalIds中但不在existingLampsTerminalIds中）
                List<Long> newLampsTerminalIds = allLampsTerminalIds.stream()
                        .filter(uniqueId -> !existingLampsTerminalIds.contains(uniqueId))
                        .collect(Collectors.toList());
                
                // 批量插入新设备
                if (!newLampsTerminalIds.isEmpty()) {
                    List<TunnelDeviceExpectedInterval> lampsIntervals = new ArrayList<>();
                    Date now = new Date();
                    for (Long uniqueId : newLampsTerminalIds) {
                        TunnelDeviceExpectedInterval interval = new TunnelDeviceExpectedInterval();
                        interval.setUniqueId(uniqueId);
                        interval.setType(1);  // type=1 表示来自tunnel_lamps_terminal
                        interval.setExpectedInterval(60);  // expected_interval=60秒（1分钟，因为灯具终端通常每分钟上报一次）
                        interval.setUpdateTime(now);
                        lampsIntervals.add(interval);
                    }
                    
                    boolean lampsSaveResult = tunnelDeviceExpectedIntervalService.saveBatch(lampsIntervals);
                    lampsNewCount = lampsIntervals.size();
                    log.info("灯具终端：发现{}个新设备，插入结果：{}", lampsNewCount, lampsSaveResult);
                } else {
                    log.info("灯具终端：没有新设备需要插入");
                }
            }
            
            int totalNewCount = edgeComputeNewCount + lampsNewCount;
            log.info("========== 期望上报间隔表刷新完成，新增{}条记录（边缘计算终端：{}条，灯具终端：{}条） ==========", 
                    totalNewCount, edgeComputeNewCount, lampsNewCount);
            
            return AjaxResult.success(String.format("期望上报间隔表刷新成功，新增记录：边缘计算终端%d条，灯具终端%d条，总计%d条", 
                    edgeComputeNewCount, lampsNewCount, totalNewCount));
        } catch (Exception e) {
            log.error("刷新期望上报间隔表失败", e);
            return AjaxResult.error("刷新期望上报间隔表失败：" + e.getMessage());
        }
    }
    
    /**
     * 统一收集指定类型的设备ID
     * 
     * @param suspectedFaultDevices 疑似异常设备列表
     * @param offlineDevices 掉线设备列表
     * @param recoveredDevices 恢复设备列表
     * @param typeId 设备类型ID（0=边缘计算终端，1=灯具终端）
     * @return 去重后的设备ID列表
     */
    private List<Long> collectDeviceIds(List<DeviceAlarmSummaryDto> suspectedFaultDevices,
                                        List<DeviceAlarmSummaryDto> offlineDevices,
                                        List<DeviceAlarmSummaryDto> recoveredDevices,
                                        int typeId) {
        List<Long> deviceIds = new ArrayList<>();
        
        // 收集疑似异常设备
        deviceIds.addAll(suspectedFaultDevices.stream()
                .filter(dto -> dto.getTypeId() == typeId)
                .map(DeviceAlarmSummaryDto::getDeviceId)
                .distinct()
                .collect(Collectors.toList()));
        
        // 收集掉线设备
        deviceIds.addAll(offlineDevices.stream()
                .filter(dto -> dto.getTypeId() == typeId)
                .map(DeviceAlarmSummaryDto::getDeviceId)
                .distinct()
                .collect(Collectors.toList()));
        
        // 收集恢复设备
        deviceIds.addAll(recoveredDevices.stream()
                .filter(dto -> dto.getTypeId() == typeId)
                .map(DeviceAlarmSummaryDto::getDeviceId)
                .distinct()
                .collect(Collectors.toList()));
        
        return deviceIds.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 批量查询边缘计算终端设备信息
     * 
     * @param deviceIds 设备ID列表
     * @return 设备ID到设备信息的Map
     */
    private Map<Long, TunnelDevicelist> batchQueryEdgeComputeDevices(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<TunnelDevicelist> devices = tunnelDevicelistService.list(
                new LambdaQueryWrapper<TunnelDevicelist>()
                        .eq(TunnelDevicelist::getDeviceTypeId, 1)
                        .in(TunnelDevicelist::getDeviceId, deviceIds)
        );
        
        log.info("批量查询边缘计算终端设备信息：{}个设备", devices.size());
        
        return devices.stream()
                .collect(Collectors.toMap(
                        TunnelDevicelist::getDeviceId,
                        device -> device,
                        (existing, replacement) -> existing
                ));
    }
    
    /**
     * 批量查询灯具终端设备信息
     * 
     * @param uniqueIds 设备唯一ID列表
     * @return 设备ID到设备信息的Map
     */
    private Map<Long, TunnelLampsTerminal> batchQueryLampsDevices(List<Long> uniqueIds) {
        if (uniqueIds == null || uniqueIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<TunnelLampsTerminal> terminals = tunnelLampsTerminalService.list(
                new LambdaQueryWrapper<TunnelLampsTerminal>()
                        .in(TunnelLampsTerminal::getUniqueId, uniqueIds)
        );
        
        log.info("批量查询灯具终端设备信息：{}个设备", terminals.size());
        
        return terminals.stream()
                .collect(Collectors.toMap(
                        TunnelLampsTerminal::getUniqueId,
                        terminal -> terminal,
                        (existing, replacement) -> existing
                ));
    }
    
    /**
     * 批量查询最新上报时间
     * 
     * @param deviceIds 设备ID列表
     * @param isEdgeCompute true=边缘计算终端，false=灯具终端
     * @return 设备ID到最新上报时间的Map
     */
    private Map<Long, Date> batchQueryLatestUploadTime(List<Long> deviceIds, boolean isEdgeCompute) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new HashMap<>();
        }
        
        if (isEdgeCompute) {
            // 查询边缘计算终端最新上报时间
            List<TunnelEdgeComputeData> latestList = tunnelEdgeComputeDataMapper.selectLatestByDeviceIds(deviceIds);
            return latestList.stream()
                    .filter(data -> data.getUploadTime() != null)
                    .collect(Collectors.toMap(
                            TunnelEdgeComputeData::getDevicelistId,
                            TunnelEdgeComputeData::getUploadTime,
                            (existing, replacement) -> existing
                    ));
        } else {
            // 查询灯具终端最新上报时间
            List<TunnelTriggerLampsData> latestList = tunnelTriggerLampsDataMapper.selectLatestByUniqueIds(deviceIds);
            return latestList.stream()
                    .filter(data -> data.getUploadTime() != null)
                    .collect(Collectors.toMap(
                            TunnelTriggerLampsData::getUniqueId,
                            TunnelTriggerLampsData::getUploadTime,
                            (existing, replacement) -> existing
                    ));
        }
    }
    
    /**
     * 计算健康状态
     * 
     * @param timeDiffSeconds 时间差（秒）
     * @param expectedInterval 期望上报间隔（秒）
     * @return 健康状态：1=按时上报（1.95倍expected_interval时间段内有上报信息），0=未按时上报（1.95倍expected_interval时间段内没有上报信息）
     */
    private int calculateHealthStatus(long timeDiffSeconds, int expectedInterval) {
        // 如果时间差在1.95倍期望间隔内，认为按时上报
        if (timeDiffSeconds <= expectedInterval * 1.95) {
            return 1; // 按时上报
        } else {
            return 0; // 未按时上报
        }
    }
}

