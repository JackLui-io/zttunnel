package com.scsdky.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.dto.TunnelLampsTerminalDto;
import com.scsdky.web.enums.DeviceZoneEnum;
import com.scsdky.web.mapper.TunnelLampsTerminalMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.ConvertBit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
//@DataSource(value = DataSourceType.SLAVE)
public class TunnelLampsTerminalServiceImpl extends ServiceImpl<TunnelLampsTerminalMapper, TunnelLampsTerminal> implements TunnelLampsTerminalService{

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    @Resource
    private TunnelSyscmdService tunnelSyscmdService;

    @Resource
    private TunnelBlueRelationNodeService tunnelBlueRelationNodeService;
    @Resource
    private TunnelLampsTerminalNodeService tunnelLampsTerminalNodeService;

    @Override
    public boolean saveObject(TunnelLampsTerminal tunnelLampsTerminal) {
        return save(tunnelLampsTerminal);
    }

    @Override
    public boolean updateObject(TunnelDevice tunnelDevice) {

        if(tunnelDevice.getUniqueId() != null ) {
            TunnelLampsTerminal tunnelLampsTerminal = getById(tunnelDevice.getUniqueId());
            return updateById(tunnelLampsTerminal);
        }else{
            throw new BaseException("灯具终端的id不存在!");
        }

    }

    @Override
    public List<TunnelLampsTerminal> getDeviceLamp(DeviceDto deviceDto) {
        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, deviceDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));

        if(tunnelDevicelistTunnelinfo == null ){
            throw new BaseException("边缘控制器不存在，无法找到对应设备!");
        }
        List<TunnelLampsTerminal> deviceLamp = getBaseMapper().getDeviceLamp(tunnelDevicelistTunnelinfo,deviceDto);
        //区段赋值
        deviceLamp.forEach(device -> {
            // ========== 新增代码：从合并的zone字段解析出zone和zone2 ==========
            // zone字段存储的是合并后的值：高4bit是zone2，低4bit是zone
            // 解析：zone = zoneValue & 0x0F (低4bit)
            //      zone2 = (zoneValue >> 4) & 0x0F (高4bit)
            if(device.getZone() != null ){
                Integer mergedZone = device.getZone();
                // 提取低4bit作为zone
                Integer zoneValue = mergedZone & 0x0F;
                // 提取高4bit作为zone2
                Integer zone2Value = (mergedZone >> 4) & 0x0F;

                Integer zoneCode = DeviceZoneEnum.getEnumCodeByBit(zoneValue);
                Integer zone2Code = DeviceZoneEnum.getEnumCodeByBit(zone2Value);

                device.setZone(zoneCode);
                device.setZone2(zone2Code);

                device.setZoneName(DeviceZoneEnum.getEnumValue(zoneCode));
                device.setZone2Name(DeviceZoneEnum.getEnumValue(zone2Code));
            } else {
                // 如果zone为null，保持原有逻辑
                if(device.getZone2() != null ){
                    Integer zone2Code = DeviceZoneEnum.getEnumCodeByBit(device.getZone2());
                    device.setZone2(zone2Code);
                    device.setZone2Name(DeviceZoneEnum.getEnumValue(zone2Code));
                }
            }
            // ========== 结束新增代码 ==========
            
            // ========== 新增代码：解析ld_whether_install字段的bit位 ==========
            // ld_whether_install字段的bit位：
            // bit 0: 是否安装雷达 (0=未安装, 1=已安装)
            // bit 1: 调光类型 (0=无级调光, 1=随车调光)
            if(device.getLdWhetherInstall() != null) {
                Integer ldWhetherInstallValue = device.getLdWhetherInstall();
                // 提取 bit 0 (是否安装雷达)
                Integer ldWhetherInstall = (ldWhetherInstallValue & (1 << 0)) != 0 ? 1 : 0;
                // 提取 bit 1 (调光类型)
                Integer dimmingType = (ldWhetherInstallValue & (1 << 1)) != 0 ? 1 : 0;
                
                // 设置解析后的值
                device.setLdWhetherInstall(ldWhetherInstall);
                device.setDimmingType(dimmingType);
            }
            // ========== 结束新增代码 ==========
            
            device.setCommunicationState(ConvertBit.computerBit(device.getLampsStatus(), 0) != 0 ? 1: 0);
            //导出excel用
            device.setCommunicationStateStr(ConvertBit.computerBit(device.getLampsStatus(), 0) != 0 ? "异常" : "正常");
            device.setWorkState(ConvertBit.computerBit(device.getLampsStatus(), 2) != 0? 1 : 0);
            //导出excel用
            device.setWorkStateStr(ConvertBit.computerBit(device.getLampsStatus(), 2) != 0? StringUtils.isNotBlank(device.getLdDeviceId())?"异常":"/" : "正常");
            int i = ConvertBit.computerBit(device.getLampsStatus(), 2) != 0 ? 1 : 0;
            device.setLdStatus(i == 0 ? "0" : "1");
            //导出excel用
            device.setLdStatusName(i == 0 ? "正常" : StringUtils.isNotBlank(device.getLdDeviceId())?"异常":"/");

            //将数值转为大桩号+小桩号
            Integer deviceNum = device.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);
            device.setDeviceNumStr(bigAndSmall);
        });
        return deviceLamp;
    }

    @Override
    public List<TunnelLampsTerminal> listDeviceLampByTunnelIds(List<Long> tunnelIds, String keyword) {
        if (CollectionUtils.isEmpty(tunnelIds)) {
            return new ArrayList<>();
        }
        Map<Long, TunnelLampsTerminal> dedup = new LinkedHashMap<>();
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setKeyword(keyword);
        for (Long tid : tunnelIds) {
            deviceDto.setTunnelId(tid);
            try {
                List<TunnelLampsTerminal> oneTunnel = getDeviceLamp(deviceDto);
                if (CollectionUtils.isEmpty(oneTunnel)) {
                    continue;
                }
                for (TunnelLampsTerminal row : oneTunnel) {
                    if (row != null && row.getUniqueId() != null) {
                        dedup.putIfAbsent(row.getUniqueId(), row);
                    }
                }
            } catch (BaseException e) {
                log.debug("批量灯具：隧道 {} 跳过（{}）", tid, e.getMessage());
            } catch (Exception e) {
                log.warn("批量灯具：隧道 {} 查询失败", tid, e);
            }
        }
        return new ArrayList<>(dedup.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dropDeviceLamp(Long id) throws JsonProcessingException {
        //删除灯具终端
        removeById(id);
        //通过id查询设备号
        TunnelLampsEdgeComputing tunnelEdgeComputing = tunnelLampsEdgeComputingService.getOne(new LambdaQueryWrapper<TunnelLampsEdgeComputing>().eq(TunnelLampsEdgeComputing::getUniqueId, id));
        //删除关联关系表
        tunnelLampsEdgeComputingService.removeById(id);

        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getDevicelistId, tunnelEdgeComputing.getDevicelistId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));

        //删除后，需要将后面的灯具序号-1
        List<TunnelLampsEdgeComputing> tunnelLampsEdgeComputings = tunnelLampsEdgeComputingService.list(new LambdaQueryWrapper<TunnelLampsEdgeComputing>()
                .gt(TunnelLampsEdgeComputing::getUniqueId, id)
                .eq(TunnelLampsEdgeComputing::getDevicelistId, tunnelEdgeComputing.getDevicelistId()));

        if(CollectionUtils.isNotEmpty(tunnelLampsEdgeComputings)) {
            List<Long> uniqueIds = tunnelLampsEdgeComputings.stream().map(TunnelLampsEdgeComputing::getUniqueId).collect(Collectors.toList());

            List<TunnelLampsTerminal> tunnelLampsTerminals = list(new LambdaQueryWrapper<TunnelLampsTerminal>()
                    .in(TunnelLampsTerminal::getUniqueId, uniqueIds));
            for (TunnelLampsTerminal tunnelLampsTerminal : tunnelLampsTerminals) {
                tunnelLampsTerminal.setPosition(tunnelLampsTerminal.getPosition() - 1);
            }
            updateBatchById(tunnelLampsTerminals);
        }

        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelDevicelistTunnelinfo.getTunnelId(),"DownloadLampListEx","","",1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceLamp(TunnelLampsTerminalDto dto) throws JsonProcessingException {
        // 验证设备号不能为空
        if (dto.getDeviceId() == null) {
            throw new BaseException("设备号不能为空！");
        }
        
        TunnelLampsTerminal tunnelLampsTerminal = new TunnelLampsTerminal();
        BeanUtils.copyProperties(dto,tunnelLampsTerminal);
        
        // ========== 重要：清除copyProperties复制的ldWhetherInstall值 ==========
        // 因为dto中的ldWhetherInstall是从数据库读取时解析出来的值（只有0或1），不是合并后的值
        // 我们需要基于dto中的原始值重新合并bit位
        tunnelLampsTerminal.setLdWhetherInstall(null);
        // ========== 结束 ==========
        
        // ========== 新增代码：区段和区段2合并为uint8 ==========
        // 将区段（zone）和区段2（zone2）合并为一个uint8值
        // 
        // 宏定义格式（来自设备通信协议）：
        // #define SET_UINT8(high4, low4) ((((high4)&0x0F)<<4) | ((low4)&0x0F))
        // 
        // 说明：
        // - high4: 高4bit，对应区段2（zone2）
        // - low4: 低4bit，对应区段（zone）
        // - 使用 & 0x0F 掩码确保只取低4bit（0-15范围）
        // 
        // 合并公式：mergedZone = ((zone2 & 0x0F) << 4) | (zone & 0x0F)
        // 示例：zone=5, zone2=10 => mergedZone = ((10 & 0x0F) << 4) | (5 & 0x0F) = 160 | 5 = 165
        Integer zone = dto.getZone();
        Integer zone2 = dto.getZone2();
        if (zone != null || zone2 != null) {
            // 如果zone或zone2为null，默认为0
            int zoneValue = (zone != null) ? zone : 0;
            int zone2Value = (zone2 != null) ? zone2 : 0;
            
            // 将无级类型值（101-110）转换为对应的基础值（1-10）
            // 例如：101(入口段1无级) -> 1, 102(入口段2无级) -> 2
            // 如果值在1-15范围内，直接使用；如果在101-110范围内，转换为对应的基础值
            if (zoneValue >= 101 && zoneValue <= 110) {
                // 无级类型值的映射：101->1, 102->2, 103->3, 104->4, 105->5, 106->6, 107->7, 108->8, 109->9, 110->10
                zoneValue = zoneValue - 100;
            }
            if (zone2Value >= 101 && zone2Value <= 110) {
                zone2Value = zone2Value - 100;
            }
            
            // 确保值在0-15范围内（4bit），使用 & 0x0F 掩码确保只取低4bit
            zoneValue = zoneValue & 0x0F;
            zone2Value = zone2Value & 0x0F;
            
            // 合并：按照宏定义格式，区段2作为高4bit，区段作为低4bit
            // SET UINT8(high4, low4) = (((high4)&0x0F)<<4) | ((low4)&0x0F)
            int mergedZone = ((zone2Value & 0x0F) << 4) | (zoneValue & 0x0F);
            
            // 调试日志：记录保存时的区段值
            log.info("保存区段值 - uniqueId: {}, zone: {} ({}), zone2: {} ({}), 合并后值: {} (二进制: {})", 
                dto.getUniqueId(),
                zoneValue, DeviceZoneEnum.getEnumValue(zoneValue),
                zone2Value, DeviceZoneEnum.getEnumValue(zone2Value),
                mergedZone, 
                String.format("%8s", Integer.toBinaryString(mergedZone)).replace(' ', '0'));
            
            tunnelLampsTerminal.setZone(mergedZone);
            // 废弃zone2字段，不再保存
            tunnelLampsTerminal.setZone2(null);
        }
        // ========== 结束新增代码 ==========
        
        //备注新增代码，开始处理设备状态
        // 转换设备状态：communicationState (Integer, 0=正常, 1=异常) -> deviceStatus (String, "00"=正常)
        if (dto.getCommunicationState() != null) {
            tunnelLampsTerminal.setDeviceStatus(dto.getCommunicationState() == 0 ? "00" : "01");
        }
        
        // 更新 lampsStatus 的 bit 位
        // bit 0: communicationState (0=正常, 1=异常)
        // bit 2: workState (0=正常, 1=异常)
        Integer currentLampsStatus = tunnelLampsTerminal.getLampsStatus();
        if (currentLampsStatus == null) {
            currentLampsStatus = 0;
        }
        
        // 设置 bit 0 (communicationState)
        if (dto.getCommunicationState() != null) {
            if (dto.getCommunicationState() == 0) {
                // 清除 bit 0: 使用 & 操作清除
                currentLampsStatus = currentLampsStatus & ~(1 << 0);
            } else {
                // 设置 bit 0: 使用 | 操作设置
                currentLampsStatus = currentLampsStatus | (1 << 0);
            }
        }
        
        // 设置 bit 2 (workState)
        if (dto.getWorkState() != null) {
            if (dto.getWorkState() == 0) {
                // 清除 bit 2: 使用 & 操作清除
                currentLampsStatus = currentLampsStatus & ~(1 << 2);
            } else {
                // 设置 bit 2: 使用 | 操作设置
                currentLampsStatus = currentLampsStatus | (1 << 2);
            }
        }
        
        tunnelLampsTerminal.setLampsStatus(currentLampsStatus);
        //结束新增
        
        // ========== 新增代码：处理ld_whether_install字段的bit位 ==========
        // ld_whether_install字段的bit位：
        // bit 0: 是否安装雷达 (0=未安装, 1=已安装)
        // bit 1: 调光类型 (0=无级调光, 1=随车调光)
        Integer currentLdWhetherInstall = 0;
        
        // 判断是新增还是编辑操作
        Long uniqueId = dto.getUniqueId();
        boolean isEdit = (uniqueId != null);
        
        // 如果是编辑操作，先获取数据库中的原始值（合并后的值）
        if (isEdit) {
            TunnelLampsTerminal existingTerminal = getById(uniqueId);
            if (existingTerminal != null && existingTerminal.getLdWhetherInstall() != null) {
                currentLdWhetherInstall = existingTerminal.getLdWhetherInstall();
            }
        }
        
        // 设置 bit 0 (是否安装雷达)
        // 注意：dto.getLdWhetherInstall() 是前端传递的值（0或1），不是合并后的值
        // 新增时：如果前端没有传递值（null），默认为0（未安装）
        // 编辑时：如果前端没有传递值（null），保持原值不变
        Integer ldWhetherInstallValue = dto.getLdWhetherInstall();
        if (ldWhetherInstallValue != null) {
            // 前端明确传递了值（0或1），按照传递的值设置
            if (ldWhetherInstallValue == 0) {
                // 清除 bit 0: 使用 & 操作清除
                currentLdWhetherInstall = currentLdWhetherInstall & ~(1 << 0);
            } else {
                // 设置 bit 0: 使用 | 操作设置
                currentLdWhetherInstall = currentLdWhetherInstall | (1 << 0);
            }
        } else {
            // 前端没有传递值（null）
            if (!isEdit) {
                // 新增时：默认为0（未安装），currentLdWhetherInstall已经是0，不需要处理
            }
            // 编辑时：保持原值不变，不需要处理
        }
        
        // 设置 bit 1 (调光类型)
        // 注意：dto.getDimmingType() 是前端传递的值（0或1），不是合并后的值
        // 新增时：如果前端没有传递值（null），默认为0（无级调光）
        // 编辑时：如果前端没有传递值（null），保持原值不变
        Integer dimmingTypeValue = dto.getDimmingType();
        if (dimmingTypeValue != null) {
            // 前端明确传递了值（0或1），按照传递的值设置
            if (dimmingTypeValue == 0) {
                // 清除 bit 1: 使用 & 操作清除
                currentLdWhetherInstall = currentLdWhetherInstall & ~(1 << 1);
            } else {
                // 设置 bit 1: 使用 | 操作设置
                currentLdWhetherInstall = currentLdWhetherInstall | (1 << 1);
            }
        } else {
            // 前端没有传递值（null）
            if (!isEdit) {
                // 新增时：默认为0（无级调光），currentLdWhetherInstall已经是0，不需要处理
            }
            // 编辑时：保持原值不变，不需要处理
        }
        
        // 重要：无论值是多少（包括0），都要明确设置，确保保存到数据库
        tunnelLampsTerminal.setLdWhetherInstall(currentLdWhetherInstall);
        
        // 调试日志：记录ld_whether_install字段的处理过程
        System.out.println("========== ld_whether_install 处理日志 ==========");
        System.out.println("操作类型: " + (isEdit ? "编辑" : "新增"));
        System.out.println("前端传递 - ldWhetherInstall: " + ldWhetherInstallValue + ", dimmingType: " + dimmingTypeValue);
        System.out.println("处理前 - currentLdWhetherInstall: " + (isEdit ? "从数据库读取" : "初始值0"));
        System.out.println("处理后 - currentLdWhetherInstall: " + currentLdWhetherInstall);
        System.out.println("解析 - bit 0(雷达): " + ((currentLdWhetherInstall & 0x01) != 0 ? "1(已安装)" : "0(未安装)"));
        System.out.println("解析 - bit 1(调光): " + ((currentLdWhetherInstall & 0x02) != 0 ? "1(随车调光)" : "0(无级调光)"));
        System.out.println("================================================");
        // ========== 结束新增代码 ==========
        Long tunnelId = tunnelLampsTerminal.getTunnelId();
        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId)
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        if(tunnelDevicelistTunnelinfo == null  ) {
            throw new BaseException("边缘控制器不存在！");
        }
        if(uniqueId != null ){
//            //批量保存隧道灯具终端节点信息
//            saveBatchNode(dto.getNodeList(), tunnelLampsTerminal.getUniqueId());
            //处理蓝牙编号 bluetoothNum 并保存
//            saveRelationNode(dto.getNodeList(),tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
            saveRelationNode(tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
            updateById(tunnelLampsTerminal);
        }else{
            //查询当前隧道id最大的灯具序号是多少，然后加1
            TunnelLampsEdgeComputing tlec = tunnelLampsEdgeComputingService.getOne(new LambdaQueryWrapper<TunnelLampsEdgeComputing>()
                    .eq(TunnelLampsEdgeComputing::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                    .orderByDesc(TunnelLampsEdgeComputing::getUniqueId)
                    .last("limit 1"));

            if(tlec != null ) {
                TunnelLampsTerminal tlt = getById(tlec.getUniqueId());
                tunnelLampsTerminal.setPosition(tlt.getPosition() + 1);
                save(tunnelLampsTerminal);
                //批量保存隧道灯具终端节点信息
//                saveBatchNode(dto.getNodeList(), tunnelLampsTerminal.getUniqueId());
                //处理蓝牙编号 bluetoothNum 并保存
//                saveRelationNode(dto.getNodeList(),tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
                saveRelationNode(tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
                updateById(tunnelLampsTerminal);
                TunnelLampsEdgeComputing tunnelLampsEdgeComputing = new TunnelLampsEdgeComputing();
                tunnelLampsEdgeComputing.setUniqueId(tunnelLampsTerminal.getUniqueId());
                tunnelLampsEdgeComputing.setDevicelistId(tunnelDevicelistTunnelinfo.getDevicelistId());
                tunnelLampsEdgeComputingService.save(tunnelLampsEdgeComputing);
            }else{
                tunnelLampsTerminal.setPosition(0);
                save(tunnelLampsTerminal);
                //批量保存隧道灯具终端节点信息
//                saveBatchNode(dto.getNodeList(), tunnelLampsTerminal.getUniqueId());
                //处理蓝牙编号 bluetoothNum 并保存
                saveRelationNode(dto.getNodeList(),tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
                saveRelationNode(tunnelLampsTerminal,tunnelDevicelistTunnelinfo.getDevicelistId());
                updateById(tunnelLampsTerminal);
                TunnelLampsEdgeComputing tunnelLampsEdgeComputing = new TunnelLampsEdgeComputing();
                tunnelLampsEdgeComputing.setUniqueId(tunnelLampsTerminal.getUniqueId());
                tunnelLampsEdgeComputing.setDevicelistId(tunnelDevicelistTunnelinfo.getDevicelistId());
                tunnelLampsEdgeComputingService.save(tunnelLampsEdgeComputing);
            }
        }
        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelLampsTerminal.getTunnelId(),"DownloadLampListEx","","",1);
        return true;
    }

    @Override
    public List<TunnelLampsTerminalNode> getDeviceLampNode(Long id) {
        LambdaQueryWrapper<TunnelLampsTerminalNode> queryWrapper = new QueryWrapper<TunnelLampsTerminalNode>()
                .lambda()
                .eq(TunnelLampsTerminalNode::getUniqueId, id)
                .orderByDesc(TunnelLampsTerminalNode::getNodeType);
        return tunnelLampsTerminalNodeService.list(queryWrapper);
    }

    @SneakyThrows
    @Override
    public Boolean updateDeviceLampNode(TunnelLampsTerminalDto dto) {
        TunnelLampsTerminal terminal = this.getById(dto.getUniqueId());
        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = baseMapper.queryTunnelDevicelistTunnelinfo(dto.getUniqueId());
        if(tunnelDevicelistTunnelinfo == null  ) {
            throw new BaseException("边缘控制器不存在！");
        }
        //批量保存隧道灯具终端节点信息
        saveBatchNode(dto.getNodeList(), terminal.getUniqueId());
        //处理蓝牙编号 bluetoothNum 并保存
        saveRelationNode(dto.getNodeList(),terminal,tunnelDevicelistTunnelinfo.getDevicelistId());
        return this.updateById(terminal);
    }

    @SneakyThrows
    @Override
    public Boolean lampIssued(Long tunnelId) {
        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelId,"DownloadLampListEx","","",1);
        return true;
    }

    /**
     * 批量保存隧道灯具终端节点信息
     * @param nodeList 节点数组
     * @param uniqueId 灯具终端id
     */
    private void saveBatchNode(List<TunnelLampsTerminalNode> nodeList,Long uniqueId) {
        if (ObjectUtils.isEmpty(nodeList)){
            return;
        }
        //删除之前蓝牙数据
        LambdaQueryWrapper<TunnelLampsTerminalNode> queryWrapper = new QueryWrapper<TunnelLampsTerminalNode>()
                .lambda()
                .eq(TunnelLampsTerminalNode::getUniqueId, uniqueId);
        tunnelLampsTerminalNodeService.remove(queryWrapper);
        //保存蓝牙列表
        nodeList.stream().forEach(item -> {
            item.setId(null);
            item.setUniqueId(uniqueId);
        });
        tunnelLampsTerminalNodeService.saveBatch(nodeList);
    }

    /**
     * 旧蓝牙节点处理（弃用）
     * @param tunnelLampsTerminal
     * @param deviceListId
     * @throws JsonProcessingException
     */
    private void saveRelationNode(TunnelLampsTerminal tunnelLampsTerminal,Long deviceListId) throws JsonProcessingException {
        String bluetoothNum = tunnelLampsTerminal.getBluetoothNum();
        if(StringUtils.isNotBlank(bluetoothNum)) {
            String[] bluetoothNums = bluetoothNum.split(",");
            //第一个为主编号
            String bluetoothNumMaster = bluetoothNums[0];
            //主编号在按照-拆分
            String[] relationNodeMasters = bluetoothNumMaster.split("-");

            // 如果格式不正确，跳过处理，不抛出异常（允许蓝牙编号为空或格式不正确）
            if (relationNodeMasters.length < 5 ) {
                return;
            }

            String relationNodeMasterOne = relationNodeMasters[0];
            String relationNodeMasterOneTwo = relationNodeMasters[1];
            String relationNodeMasterOneThree = relationNodeMasters[2];
            String relationNodeMasterFour = relationNodeMasters[3];
            String relationNodeMasterFive = relationNodeMasters[4];

            tunnelLampsTerminal.setBluetoothValue(relationNodeMasterOneTwo + "," + relationNodeMasterOneThree);
            tunnelLampsTerminal.setCH1(relationNodeMasterOneTwo + "," + relationNodeMasterFour);
            tunnelLampsTerminal.setCH2(relationNodeMasterOneThree + "," + relationNodeMasterFive);
            /**
             * 构建新的格式
             */
            JSONObject result = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            result.put("Relation",jsonArr);

            JSONObject jsonObjectChild = new JSONObject();
            jsonObjectChild.put("MasterLampNo",relationNodeMasterOne);
            jsonObjectChild.put("Wait", Arrays.asList(Integer.valueOf(relationNodeMasterOneTwo),Integer.valueOf(relationNodeMasterOneThree)));
            jsonObjectChild.put("Keep",Arrays.asList(Integer.valueOf(relationNodeMasterFour),Integer.valueOf(relationNodeMasterFive)));

            JSONArray jsonArrLampsArr = new JSONArray();
            jsonObjectChild.put("Lamps",jsonArrLampsArr);

            jsonArr.add(jsonObjectChild);
            //保存之前先删除以前的
            tunnelBlueRelationNodeService.remove(Wrappers.lambdaQuery(TunnelBlueRelationNode.class)
                    .eq(TunnelBlueRelationNode::getMasterNode,relationNodeMasterOne)
                    .eq(TunnelBlueRelationNode::getDeviceListId,deviceListId));
            getTunnelBlueRelationNodes(bluetoothNums,jsonArrLampsArr,deviceListId,relationNodeMasterOne);

            TunnelBlueRelationNode tunnelBlueRelationNode = new TunnelBlueRelationNode();
            tunnelBlueRelationNode.setDeviceListId(deviceListId);
            tunnelBlueRelationNode.setRelationJson(JSON.toJSONString(result));
            tunnelBlueRelationNode.setMasterNode(Integer.valueOf(relationNodeMasterOne));
            tunnelBlueRelationNodeService.save(tunnelBlueRelationNode);

            //指令下发
            //List<Integer> relationNodes  = tunnelBlueRelationNodes.stream().map(TunnelBlueRelationNode::getRelationNode).collect(Collectors.toList());
            //String relationNodeStr = JSON.toJSONString(relationNodes);
            //记录下发指令
            //tunnelSyscmdService.setCmdData(tunnelLampsTerminal.getTunnelId(),"DownloadLampRelationList",String.valueOf(tunnelLampsTerminal.getDeviceId()),relationNodeStr,1);
            //大于1才下发
            if(bluetoothNums.length > 1) {
                tunnelSyscmdService.setCmdData(tunnelLampsTerminal.getTunnelId(),"DownloadLampRelationList","","",1);
            }
        }
    }

    /**
     * 蓝牙节点处理
     * @param nodeList 节点数组
     * @param tunnelLampsTerminal 灯具终端对象
     * @param deviceListId 边缘控制器id
     * @throws JsonProcessingException
     */
    private void saveRelationNode(List<TunnelLampsTerminalNode> nodeList,TunnelLampsTerminal tunnelLampsTerminal,Long deviceListId) throws JsonProcessingException {
        if (ObjectUtils.isEmpty(nodeList) || ObjectUtils.isEmpty(tunnelLampsTerminal) | ObjectUtils.isEmpty(deviceListId)){
            return;
        }
        for (TunnelLampsTerminalNode tunnelLampsTerminalNode : nodeList) {
            if (ObjectUtils.isEmpty(tunnelLampsTerminalNode.getNodeType()) || tunnelLampsTerminalNode.getNodeType().equals(0)){
                continue;
            }
            String[] bluetoothNums = tunnelLampsTerminalNode.getParams().split(",");
            //第一个为主编号
            String bluetoothNumMaster = bluetoothNums[0];
            //主编号在按照-拆分
            String[] relationNodeMasters = bluetoothNumMaster.split("-");
            // 如果格式不正确，跳过当前节点处理
            if (relationNodeMasters.length < 5 ) {
                continue;
            }
            String relationNodeMasterOne = relationNodeMasters[0];
            String relationNodeMasterOneTwo = relationNodeMasters[1];
            String relationNodeMasterOneThree = relationNodeMasters[2];
            String relationNodeMasterFour = relationNodeMasters[3];
            String relationNodeMasterFive = relationNodeMasters[4];
            tunnelLampsTerminal.setBluetoothValue(relationNodeMasterOneTwo + "," + relationNodeMasterOneThree);
            tunnelLampsTerminal.setCH1(relationNodeMasterOneTwo + "," + relationNodeMasterFour);
            tunnelLampsTerminal.setCH2(relationNodeMasterOneThree + "," + relationNodeMasterFive);
            /**
             * 构建新的格式
             */
            JSONObject result = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            result.put("Relation",jsonArr);

            JSONObject jsonObjectChild = new JSONObject();
            jsonObjectChild.put("MasterLampNo",relationNodeMasterOne);
            jsonObjectChild.put("Wait", Arrays.asList(Integer.valueOf(relationNodeMasterOneTwo),Integer.valueOf(relationNodeMasterOneThree)));
            jsonObjectChild.put("Keep",Arrays.asList(Integer.valueOf(relationNodeMasterFour),Integer.valueOf(relationNodeMasterFive)));

            JSONArray jsonArrLampsArr = new JSONArray();
            jsonObjectChild.put("Lamps",jsonArrLampsArr);

            jsonArr.add(jsonObjectChild);
            //保存之前先删除以前的
            tunnelBlueRelationNodeService.remove(Wrappers.lambdaQuery(TunnelBlueRelationNode.class)
                    .eq(TunnelBlueRelationNode::getMasterNode,relationNodeMasterOne)
                    .eq(TunnelBlueRelationNode::getDeviceListId,deviceListId));
            getTunnelBlueRelationNodes(bluetoothNums,jsonArrLampsArr,deviceListId,relationNodeMasterOne);

            TunnelBlueRelationNode tunnelBlueRelationNode = new TunnelBlueRelationNode();
            tunnelBlueRelationNode.setDeviceListId(deviceListId);
            tunnelBlueRelationNode.setRelationJson(JSON.toJSONString(result));
            tunnelBlueRelationNode.setMasterNode(Integer.valueOf(relationNodeMasterOne));
            tunnelBlueRelationNodeService.save(tunnelBlueRelationNode);
            //大于1才下发
            if(bluetoothNums.length > 1) {
                tunnelSyscmdService.setCmdData(tunnelLampsTerminal.getTunnelId(),"DownloadLampRelationList","","",1);
            }
        }
    }

    /**
     * 获取子节点数据
     * @param bluetoothNums
     * @param jsonArrLampsArr lamps数组
     * @return
     */
    private void getTunnelBlueRelationNodes(String[] bluetoothNums, JSONArray jsonArrLampsArr,Long deviceListId,String masterNode) {
        List<String> slaveNodes = new ArrayList<>();
        //子节点从第一个开始
        for (int i = 1; i < bluetoothNums.length; i++) {
            String bluetoothNumSlave = bluetoothNums[i];
            //主编号在按照-拆分
            String[] relationNodeSlaves = bluetoothNumSlave.split("-");
            // 如果格式不正确，跳过当前子节点处理
            if (relationNodeSlaves.length < 5 ) {
                continue;
            }
            String relationNodeSlaveOne = relationNodeSlaves[0];
            String relationNodeSlaveTwo = relationNodeSlaves[1];
            String relationNodeSlaveThree = relationNodeSlaves[2];
            String relationNodeSlaveFour = relationNodeSlaves[3];
            String relationNodeSlaveFive = relationNodeSlaves[4];
            //添加子节点
            slaveNodes.add(relationNodeSlaveOne);
            JSONObject jsonObject = new JSONObject();
            List<Integer> wait = new ArrayList<>();
            List<Integer> keep = new ArrayList<>();
            wait.add(Integer.valueOf(relationNodeSlaveTwo));
            wait.add(Integer.valueOf(relationNodeSlaveThree));
            keep.add(Integer.valueOf(relationNodeSlaveFour));
            keep.add(Integer.valueOf(relationNodeSlaveFive));
            jsonObject.put("LampNO",relationNodeSlaveOne);
            jsonObject.put("Wait",wait);
            jsonObject.put("Keep",keep);
            jsonArrLampsArr.add(jsonObject);
        }
        //查询子节点里面是否包含了以前的主节点，如果包含了提示
        /*if(CollectionUtils.isNotEmpty(slaveNodes)){
            List<TunnelBlueRelationNode> tunnelBlueRelationNodes = tunnelBlueRelationNodeService.list(Wrappers.lambdaQuery(TunnelBlueRelationNode.class)
                    .in(TunnelBlueRelationNode::getMasterNode, slaveNodes)
                    .eq(TunnelBlueRelationNode::getDeviceListId, deviceListId));

            if(CollectionUtils.isNotEmpty(tunnelBlueRelationNodes)) {
                String nodes = tunnelBlueRelationNodes.stream().map(TunnelBlueRelationNode -> String.valueOf(TunnelBlueRelationNode.getMasterNode())).collect(Collectors.joining(","));
                throw new BaseException("子节点被主节点使用,不能添加！主节点使用的节点为: "  + nodes);
            }
            //如果子节点包含了主节点，也不能添加
            if(slaveNodes.contains(masterNode)) {
                throw new BaseException("子节点不能和主节点一样!");
            }
        }*/

    }
}




