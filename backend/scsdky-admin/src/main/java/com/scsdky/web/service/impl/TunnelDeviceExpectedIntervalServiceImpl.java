package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelDeviceExpectedInterval;
import com.scsdky.web.mapper.TunnelDeviceExpectedIntervalMapper;
import com.scsdky.web.service.TunnelDeviceExpectedIntervalService;
import org.springframework.stereotype.Service;

/**
 * 设备期望间隔时间表 服务实现类
 * 
 * @author system
 */
@Service
public class TunnelDeviceExpectedIntervalServiceImpl 
        extends ServiceImpl<TunnelDeviceExpectedIntervalMapper, TunnelDeviceExpectedInterval>
        implements TunnelDeviceExpectedIntervalService {
    
}

