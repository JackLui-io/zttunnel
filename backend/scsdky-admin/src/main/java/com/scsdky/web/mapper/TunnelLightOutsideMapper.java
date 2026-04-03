package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelLightOutside;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.LightCountVo;
import com.scsdky.web.domain.vo.monitor.LightOutsideVo;

import java.util.List;
import java.util.Map;

/**
 * @Entity generator.domain.TunnelLightOutside
 */
public interface TunnelLightOutsideMapper extends BaseMapper<TunnelLightOutside> {

    /**
     * 洞内外照度统计
     * @param analyzeDto
     * @return
     */
    List<InsideAndOutsideVo> insideAndOutside(AnalyzeDto analyzeDto);

    /**
     * 每小时统计
     * @param analyzeDto
     * @return
     */
    List<LightOutsideVo> zdByHouse(AnalyzeDto analyzeDto);

    LightCountVo getAvgData(AnalyzeDto analyzeDto);
}




