package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.TunnelPowerData;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import com.scsdky.web.service.TunnelPowerDataService;
import com.scsdky.web.mapper.TunnelPowerDataMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class TunnelPowerDataServiceImpl extends ServiceImpl<TunnelPowerDataMapper, TunnelPowerData>
    implements TunnelPowerDataService{

    @Override
    public List<DnVo> getPowerDataValue(List<Long> ascUniqueIds, List<Long> descUniqueIds,String nowDate) {
        return baseMapper.getPowerDataValue(ascUniqueIds,descUniqueIds,nowDate);
    }

    @Override
    public List<EnergyCarbonVo> selectCountValue(List<Long> ascUniqueIds, AnalyzeDto analyzeDto) {
        return baseMapper.selectCountValue(ascUniqueIds,analyzeDto);
    }

    @Override
    public List<PowerLightVo> selectImepGroupByMonth(String year, List<Long> ascUniqueIds) {
        return baseMapper.selectImepGroupByMonth(year,ascUniqueIds);
    }
}




