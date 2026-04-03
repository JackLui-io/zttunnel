package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelCarbonDayPush;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.AddrEnergyCarbonVo;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.mapper.TunnelCarbonDayPushMapper;
import com.scsdky.web.service.TunnelCarbonDayPushService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 涂波
 * @description 针对表【tunnel_carbon_day_push(隧道能碳每日数据)】的数据库操作Service实现
 * @createDate 2025-06-23 10:52:20
 */
@Service
public class TunnelCarbonDayPushServiceImpl extends ServiceImpl<TunnelCarbonDayPushMapper, TunnelCarbonDayPush>
        implements TunnelCarbonDayPushService{

    @Override
    public List<EnergyCarbonVo> getEnergyCarbonVo(List<Long> devicelistId, List<Long> addrIds, AnalyzeDto analyzeDto) {
        return baseMapper.getEnergyCarbonVo(devicelistId,addrIds,analyzeDto);
    }

    @Override
    public List<AddrEnergyCarbonVo> getEnergyCarbonVo4(List<Long> devicelistId, List<Long> addrIds, AnalyzeDto analyzeDto) {
        return baseMapper.getEnergyCarbonVo4(devicelistId,addrIds,analyzeDto);
    }

    @Override
    public List<AddrEnergyCarbonVo> getLatestDataByAddrIds(List<Long> devicelistId, List<Long> addrIds,AnalyzeDto analyzeDto) {
        return baseMapper.getLatestDataByAddrIds(devicelistId,addrIds,analyzeDto);
    }
}




