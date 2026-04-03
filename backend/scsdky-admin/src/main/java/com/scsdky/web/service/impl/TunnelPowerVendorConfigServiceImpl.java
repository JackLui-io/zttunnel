package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelPowerVendorConfig;
import com.scsdky.web.mapper.TunnelPowerVendorConfigMapper;
import com.scsdky.web.service.TunnelPowerVendorConfigService;
import org.springframework.stereotype.Service;

/**
 * 厂商配置 Service 实现
 */
@Service
public class TunnelPowerVendorConfigServiceImpl extends ServiceImpl<TunnelPowerVendorConfigMapper, TunnelPowerVendorConfig>
        implements TunnelPowerVendorConfigService {
}
