package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelEnergyCarbon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;

import java.util.List;

/**
 * @Entity generator.domain.TunnelEnergyCarbon
 */
public interface TunnelEnergyCarbonMapper extends BaseMapper<TunnelEnergyCarbon> {

    List<EnergyCarbonVo> carbon(AnalyzeDto analyzeDto);
}




