package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.mapper.TunnelDeviceBluetoothMapper;
import com.scsdky.web.domain.TunnelDeviceBluetooth;
import com.scsdky.web.service.TunnelDeviceBluetoothService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 边缘控制器蓝牙节点信息(TunnelDeviceBluetooth)表服务实现类
 *
 * @author makejava
 * @since 2025-09-18 15:44:06
 */
@Service("tunnelDeviceBluetoothService")
public class TunnelDeviceBluetoothServiceImpl extends ServiceImpl<TunnelDeviceBluetoothMapper, TunnelDeviceBluetooth> implements TunnelDeviceBluetoothService {

    @Override
    public List<TunnelDeviceBluetooth> selectList(Long devicelistId) {
        LambdaQueryWrapper<TunnelDeviceBluetooth> queryWrapper = new QueryWrapper<TunnelDeviceBluetooth>()
                .lambda()
                .eq(TunnelDeviceBluetooth::getDevicelistId, devicelistId);
        return this.list(queryWrapper);
    }
}

