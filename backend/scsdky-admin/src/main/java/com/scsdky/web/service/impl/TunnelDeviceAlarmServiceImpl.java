package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelDeviceAlarm;
import com.scsdky.web.mapper.TunnelDeviceAlarmMapper;
import com.scsdky.web.service.TunnelDeviceAlarmService;
import org.springframework.stereotype.Service;

/**
 * 设备巡检告警记录表 服务实现类
 * 
 * @author system
 */
@Service
public class TunnelDeviceAlarmServiceImpl 
        extends ServiceImpl<TunnelDeviceAlarmMapper, TunnelDeviceAlarm>
        implements TunnelDeviceAlarmService {
    
}

