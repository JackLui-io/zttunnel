package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelPower;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface TunnelPowerService extends IService<TunnelPower> {

    List<TunnelPower> getDevicePower(Long tunnelId);
}
