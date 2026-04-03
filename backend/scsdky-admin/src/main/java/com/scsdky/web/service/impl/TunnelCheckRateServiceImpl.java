package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelCheckRate;
import com.scsdky.web.mapper.TunnelCheckRateMapper;
import com.scsdky.web.service.TunnelCheckRateService;
import org.springframework.stereotype.Service;

/**
 * 设备在线率阈值配置 Service 实现类
 */
@Service
public class TunnelCheckRateServiceImpl extends ServiceImpl<TunnelCheckRateMapper, TunnelCheckRate> implements TunnelCheckRateService {
}
