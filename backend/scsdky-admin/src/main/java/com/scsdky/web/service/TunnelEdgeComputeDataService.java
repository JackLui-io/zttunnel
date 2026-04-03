package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface TunnelEdgeComputeDataService extends IService<TunnelEdgeComputeData> {

    /**
     * 获取亮灯时间
     * @param analyzeDto
     * @return
     */
    List<Map<String,Object>> getLightTime(AnalyzeDto analyzeDto);
}
