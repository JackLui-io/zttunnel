package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelCheckRate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备在线率阈值配置 Mapper
 */
@Mapper
public interface TunnelCheckRateMapper extends BaseMapper<TunnelCheckRate> {
}
