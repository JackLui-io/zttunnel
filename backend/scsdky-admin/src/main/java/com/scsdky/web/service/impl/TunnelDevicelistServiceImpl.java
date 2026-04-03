package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.TunnelDevicelistVo;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.web.mapper.TunnelCarbonDayMapper;
import com.scsdky.web.mapper.TunnelCarbonDayPushMapper;
import com.scsdky.web.mapper.TunnelDeviceBluetoothMapper;
import com.scsdky.web.mapper.TunnelDeviceParamMapper;
import com.scsdky.web.mapper.TunnelDevicelistMapper;
import com.scsdky.web.mapper.TunnelDevicelistTunnelinfoMapper;
import com.scsdky.web.mapper.TunnelEdgeComputeDataMapper;
import com.scsdky.web.mapper.TunnelEdgeTriggerDataMapper;
import com.scsdky.web.mapper.TunnelInsideOutsideDayMapper;
import com.scsdky.web.mapper.TunnelLampsEdgeComputingMapper;
import com.scsdky.web.mapper.TunnelOutOfRadarMapper;
import com.scsdky.web.mapper.TunnelPowerDataMapper;
import com.scsdky.web.mapper.TunnelPowerEdgeComputingMapper;
import com.scsdky.web.mapper.TunnelPowerMapper;
import com.scsdky.web.mapper.TunnelTrafficFlowDayMapper;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelEdgeComputeDataService;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import com.scsdky.web.utils.ConvertBit;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 边缘计算终端基本表--唐总
 */
@Slf4j
@Service
//@DataSource(value = DataSourceType.SLAVE)
public class TunnelDevicelistServiceImpl extends ServiceImpl<TunnelDevicelistMapper, TunnelDevicelist> implements TunnelDevicelistService {

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    @Resource
    private TunnelOutOfRadarMapper tunnelOutOfRadarMapper;
    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;
    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;
    @Resource
    private TunnelDevicelistTunnelinfoMapper tunnelDevicelistTunnelinfoMapper;
    @Resource
    private TunnelEdgeComputeDataMapper tunnelEdgeComputeDataMapper;
    @Resource
    private TunnelEdgeTriggerDataMapper tunnelEdgeTriggerDataMapper;
    @Resource
    private TunnelPowerEdgeComputingMapper tunnelPowerEdgeComputingMapper;
    @Resource
    private TunnelDeviceBluetoothMapper tunnelDeviceBluetoothMapper;
    @Resource
    private TunnelDeviceParamMapper tunnelDeviceParamMapper;
    @Resource
    private TunnelCarbonDayPushMapper tunnelCarbonDayPushMapper;
    @Resource
    private TunnelCarbonDayMapper tunnelCarbonDayMapper;
    @Resource
    private TunnelInsideOutsideDayMapper tunnelInsideOutsideDayMapper;
    @Resource
    private TunnelLampsEdgeComputingMapper tunnelLampsEdgeComputingMapper;
    @Resource
    private TunnelPowerMapper tunnelPowerMapper;
    @Resource
    private TunnelPowerDataMapper tunnelPowerDataMapper;
    @Resource
    private TunnelTrafficFlowDayMapper tunnelTrafficFlowDayMapper;

    //边缘控制器类型
    private static final int EDGE_CONTROLLER_TYPE = 1;

    @Override
    public boolean saveObject(TunnelDevicelist tunnelDevicelist) {
        return save(tunnelDevicelist);
    }

    @Override
    public TunnelDevicelist getOneObject(String deviceId) {
        return getById(deviceId);
    }

    @Override
    public boolean updateObject(TunnelDevice tunnelDevice) {
        TunnelDevicelist tunnelDevicelist = new TunnelDevicelist();
        tunnelDevicelist.setDeviceId(Long.valueOf(tunnelDevice.getDeviceId()));
        tunnelDevicelist.setNickName(tunnelDevice.getDeviceName());
        tunnelDevicelist.setCsq(tunnelDevice.getCsq());
        tunnelDevicelist.setLastUpdate(new Date());
        tunnelDevicelist.setDevicePassword(tunnelDevice.getDevicePassword());
        return updateById(tunnelDevicelist);
    }

    @Override
    public List<TunnelDevicelistVo> getDevicelist(DeviceDto deviceDto) {
        try {
            return getTunnelDevicelist(deviceDto.getTunnelId(), deviceDto.getType());
        } catch (Exception e) {
            // 记录异常日志，避免接口完全失败
            log.error("获取设备列表失败，tunnelId: {}, type: {}", deviceDto.getTunnelId(), deviceDto.getType(), e);
            // 返回空列表而不是抛出异常，避免前端请求失败
            return new ArrayList<>();
        }
    }

    @Override
    public List<TunnelDevicelistVo> listDevicelistByTunnelIds(List<Long> tunnelIds, Integer type) {
        if (CollectionUtils.isEmpty(tunnelIds) || type == null) {
            return new ArrayList<>();
        }
        Map<Long, TunnelDevicelistVo> dedup = new LinkedHashMap<>();
        for (Long tid : tunnelIds) {
            try {
                for (TunnelDevicelistVo vo : getTunnelDevicelist(tid, type)) {
                    if (vo != null && vo.getDeviceId() != null) {
                        dedup.putIfAbsent(vo.getDeviceId(), vo);
                    }
                }
            } catch (Exception e) {
                log.warn("批量获取 devicelist 单隧道失败 tunnelId={}, type={}", tid, type, e);
            }
        }
        return new ArrayList<>(dedup.values());
    }

    private List<TunnelDevicelistVo> getTunnelDevicelist(Long tunnelId, Integer type) {
        List<TunnelDevicelistTunnelinfo> tunnelDevicelistTunnelinfos = tunnelDevicelistTunnelinfoService.list(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId)
                .eq(TunnelDevicelistTunnelinfo::getType, type));
        if(CollectionUtils.isNotEmpty(tunnelDevicelistTunnelinfos) ){
            List<Long> deviceListIds = tunnelDevicelistTunnelinfos.stream().map(TunnelDevicelistTunnelinfo::getDevicelistId).collect(Collectors.toList());
            List<TunnelDevicelist> tunnelDevicelist = list(new LambdaQueryWrapper<TunnelDevicelist>()
                    .in(TunnelDevicelist::getDeviceId, deviceListIds)
                    .eq(TunnelDevicelist::getDeviceTypeId, type));
            //转换返回对象
            List<TunnelDevicelistVo> list = getDevicelistVoList(tunnelDevicelist);
            //边缘控制器要加上雷达及亮度仪状态
            if(type.equals(EDGE_CONTROLLER_TYPE)){
                setEdgeControllerStatus( list);
            }else {
                //电能终端要加上电表数量
                setElectricityMeterNum(list);
            }
            return list != null ? list : new ArrayList<>();
        }else{
            return new ArrayList<>();
        }
    }

    /**
     * 电能终端要加上电表数量
     * @param list 边缘计算终端数组
     */
    private void setElectricityMeterNum(List<TunnelDevicelistVo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        
        //电能终端要加上电表数量
        for (TunnelDevicelistVo vo : list) {
            try {
                List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.powerList(vo.getDeviceId(), null);
                vo.setElectricityMeterNum(tunnelPowerEdgeComputings != null ? tunnelPowerEdgeComputings.size() : 0);
            } catch (Exception e) {
                // 单个设备电表数量查询失败不影响其他设备，记录日志并设置默认值
                log.warn("获取电表数量失败，deviceId: {}", vo.getDeviceId(), e);
                vo.setElectricityMeterNum(0);
            }
        }
    }

    /**
     * 边缘控制器设置雷达及亮度仪状态
     * @param list 边缘计算终端数组
     */
    private void setEdgeControllerStatus(List<TunnelDevicelistVo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        
        for (TunnelDevicelistVo vo : list) {
            try {
                LambdaQueryWrapper<TunnelOutOfRadar> wrapper = new QueryWrapper<TunnelOutOfRadar>()
                        .lambda()
                        .eq(TunnelOutOfRadar::getDevicelistId, vo.getDeviceId());
                List<TunnelOutOfRadar> tunnelOutOfRadars = tunnelOutOfRadarMapper.selectList(wrapper);
                
                if (CollectionUtils.isEmpty(tunnelOutOfRadars)) {
                    continue;
                }
                
                for (TunnelOutOfRadar tunnelOutOfRadar : tunnelOutOfRadars) {
                    try {
                        //通过边缘控制器去拿到最新上传的一条数据
                        TunnelEdgeComputeData tunnelEdgeComputeData = tunnelEdgeComputeDataService.getOne(Wrappers.lambdaQuery(TunnelEdgeComputeData.class)
                                .eq(TunnelEdgeComputeData::getDevicelistId, tunnelOutOfRadar.getDevicelistId())
                                .orderByDesc(TunnelEdgeComputeData::getUploadTime).last("limit 1"));
                        
                        if(tunnelEdgeComputeData != null && tunnelEdgeComputeData.getEdgeComputeStatus0() != null) {
                            Integer edgeComputeStatus0 = tunnelEdgeComputeData.getEdgeComputeStatus0();
                            //洞外亮度义
                            if (tunnelOutOfRadar.getType() == 2) {
                                vo.setBrightnessMeterStatus(ConvertBit.computerBit(edgeComputeStatus0, 0) != 0 ? "异常": "正常");
                            }
                            //洞外雷达
                            if (tunnelOutOfRadar.getType() == 1) {
                                vo.setRadarStatus(ConvertBit.computerBit(edgeComputeStatus0, 1) != 0 ? "异常": "正常");
                            }
                        }
                    } catch (Exception e) {
                        // 单个设备状态查询失败不影响其他设备，记录日志继续处理
                        log.warn("获取设备状态失败，deviceId: {}, devicelistId: {}", vo.getDeviceId(), tunnelOutOfRadar.getDevicelistId(), e);
                    }
                }
            } catch (Exception e) {
                // 单个设备处理失败不影响其他设备，记录日志继续处理
                log.warn("设置边缘控制器状态失败，deviceId: {}", vo.getDeviceId(), e);
            }
        }
    }

    /**
     * 转换响应对象
     * @param tunnelDevicelist 边缘计算终端数组
     * @return
     */
    private static List<TunnelDevicelistVo> getDevicelistVoList(List<TunnelDevicelist> tunnelDevicelist) {
        List<TunnelDevicelistVo> list = tunnelDevicelist.stream().map(item -> {
            TunnelDevicelistVo vo = new TunnelDevicelistVo();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rebindDeviceId(Long oldDeviceId, Long newDeviceId) {
        if (oldDeviceId == null || newDeviceId == null) {
            throw new ServiceException("设备号不能为空");
        }
        if (oldDeviceId.equals(newDeviceId)) {
            return true;
        }
        TunnelDevicelist oldRow = getById(oldDeviceId);
        if (oldRow == null) {
            throw new ServiceException("原设备不存在");
        }
        if (oldRow.getDeviceTypeId() == null || !oldRow.getDeviceTypeId().equals(EDGE_CONTROLLER_TYPE)) {
            throw new ServiceException("仅允许对边缘控制器（device_type_id=1）执行设备号替换");
        }
        if (getById(newDeviceId) != null) {
            throw new ServiceException("新设备号已在 tunnel_devicelist 中存在，请更换为未占用的设备号");
        }

        tunnelDevicelistTunnelinfoMapper.update(null, Wrappers.lambdaUpdate(TunnelDevicelistTunnelinfo.class)
                .set(TunnelDevicelistTunnelinfo::getDevicelistId, newDeviceId)
                .eq(TunnelDevicelistTunnelinfo::getDevicelistId, oldDeviceId));
        tunnelOutOfRadarMapper.update(null, Wrappers.lambdaUpdate(TunnelOutOfRadar.class)
                .set(TunnelOutOfRadar::getDevicelistId, newDeviceId)
                .eq(TunnelOutOfRadar::getDevicelistId, oldDeviceId));
        tunnelEdgeComputeDataMapper.update(null, Wrappers.lambdaUpdate(TunnelEdgeComputeData.class)
                .set(TunnelEdgeComputeData::getDevicelistId, newDeviceId)
                .eq(TunnelEdgeComputeData::getDevicelistId, oldDeviceId));
        tunnelEdgeTriggerDataMapper.update(null, Wrappers.lambdaUpdate(TunnelEdgeTriggerData.class)
                .set(TunnelEdgeTriggerData::getDevicelistId, newDeviceId)
                .eq(TunnelEdgeTriggerData::getDevicelistId, oldDeviceId));
        tunnelPowerEdgeComputingMapper.update(null, Wrappers.lambdaUpdate(TunnelPowerEdgeComputing.class)
                .set(TunnelPowerEdgeComputing::getDevicelistId, newDeviceId)
                .eq(TunnelPowerEdgeComputing::getDevicelistId, oldDeviceId));
        tunnelDeviceBluetoothMapper.update(null, Wrappers.lambdaUpdate(TunnelDeviceBluetooth.class)
                .set(TunnelDeviceBluetooth::getDevicelistId, newDeviceId)
                .eq(TunnelDeviceBluetooth::getDevicelistId, oldDeviceId));
        tunnelDeviceParamMapper.update(null, Wrappers.lambdaUpdate(TunnelDeviceParam.class)
                .set(TunnelDeviceParam::getDevicelistId, newDeviceId)
                .eq(TunnelDeviceParam::getDevicelistId, oldDeviceId));
        tunnelCarbonDayPushMapper.update(null, Wrappers.lambdaUpdate(TunnelCarbonDayPush.class)
                .set(TunnelCarbonDayPush::getDevicelistId, newDeviceId)
                .eq(TunnelCarbonDayPush::getDevicelistId, oldDeviceId));
        tunnelCarbonDayMapper.update(null, Wrappers.lambdaUpdate(TunnelCarbonDay.class)
                .set(TunnelCarbonDay::getDevicelistId, newDeviceId)
                .eq(TunnelCarbonDay::getDevicelistId, oldDeviceId));
        tunnelInsideOutsideDayMapper.update(null, Wrappers.lambdaUpdate(TunnelInsideOutsideDay.class)
                .set(TunnelInsideOutsideDay::getDevicelistId, newDeviceId)
                .eq(TunnelInsideOutsideDay::getDevicelistId, oldDeviceId));
        tunnelLampsEdgeComputingMapper.update(null, Wrappers.lambdaUpdate(TunnelLampsEdgeComputing.class)
                .set(TunnelLampsEdgeComputing::getDevicelistId, newDeviceId)
                .eq(TunnelLampsEdgeComputing::getDevicelistId, oldDeviceId));
        tunnelPowerMapper.update(null, Wrappers.lambdaUpdate(TunnelPower.class)
                .set(TunnelPower::getDevicelistId, newDeviceId)
                .eq(TunnelPower::getDevicelistId, oldDeviceId));
        tunnelPowerDataMapper.update(null, Wrappers.lambdaUpdate(TunnelPowerData.class)
                .set(TunnelPowerData::getDevicelistId, newDeviceId)
                .eq(TunnelPowerData::getDevicelistId, oldDeviceId));
        tunnelTrafficFlowDayMapper.update(null, Wrappers.lambdaUpdate(TunnelTrafficFlowDay.class)
                .set(TunnelTrafficFlowDay::getDevicelistId, newDeviceId)
                .eq(TunnelTrafficFlowDay::getDevicelistId, oldDeviceId));

        int pk = baseMapper.updateDevicePrimaryKey(oldDeviceId, newDeviceId);
        if (pk != 1) {
            throw new ServiceException("更新 tunnel_devicelist 主键失败，请检查数据或外键约束");
        }
        return true;
    }
}




