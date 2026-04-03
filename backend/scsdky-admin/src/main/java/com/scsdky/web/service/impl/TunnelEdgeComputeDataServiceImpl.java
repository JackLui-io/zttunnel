package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.mapper.TunnelEdgeComputeDataMapper;
import com.scsdky.web.service.TunnelEdgeComputeDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class TunnelEdgeComputeDataServiceImpl extends ServiceImpl<TunnelEdgeComputeDataMapper, TunnelEdgeComputeData>
    implements TunnelEdgeComputeDataService{

    @Override
    public List<Map<String,Object>> getLightTime(AnalyzeDto analyzeDto) {

        return getBaseMapper().getLightTime(analyzeDto);
    }
}




