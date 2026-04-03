package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelEleMeter;
import com.scsdky.web.domain.dto.AnalyzeDto;

import java.util.List;

public interface TunnelEleMeterMapper extends BaseMapper<TunnelEleMeter> {

    List<TunnelEleMeter> selectEleMeterByTunnelId(AnalyzeDto analyzeDto);
}
