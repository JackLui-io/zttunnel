package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelDeviceBluetooth;

import java.util.List;

/**
 * 边缘控制器蓝牙节点信息(TunnelDeviceBluetooth)表服务接口
 *
 * @author makejava
 * @since 2025-09-18 15:44:06
 */
public interface TunnelDeviceBluetoothService extends IService<TunnelDeviceBluetooth> {

    List<TunnelDeviceBluetooth> selectList(Long devicelistId);
}

