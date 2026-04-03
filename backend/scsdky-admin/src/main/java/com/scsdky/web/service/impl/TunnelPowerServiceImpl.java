package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelPower;
import com.scsdky.web.mapper.TunnelPowerMapper;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelPowerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class TunnelPowerServiceImpl extends ServiceImpl<TunnelPowerMapper, TunnelPower>
    implements TunnelPowerService{

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Override
    public List<TunnelPower> getDevicePower(Long tunnelId) {

        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>().eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId));
        if(tunnelDevicelistTunnelinfo == null ){
            throw new BaseException("边缘控制器不存在，无法找到对应设备!");
        }

        return list(new LambdaQueryWrapper<TunnelPower>().eq(TunnelPower::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId()));
    }
}




