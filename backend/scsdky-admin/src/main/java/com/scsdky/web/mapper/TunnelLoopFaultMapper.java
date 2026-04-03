package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelLoopFault;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.LoopDto;
import com.scsdky.web.domain.vo.LoopCountVo;

import java.util.List;

/**
 * @Entity generator.domain.TTunnelLoopFault
 */
public interface TunnelLoopFaultMapper extends BaseMapper<TunnelLoopFault> {


    /**
     * 回路故障列表
     * @param loopDto
     * @return
     */
    List<TunnelLoopFault> getLoopListByPage(LoopDto loopDto);

    /**
     * 列表查询
     * @param loopDto
     * @return
     */
    List<TunnelLoopFault> getLoopList(LoopDto loopDto);

    List<LoopCountVo> getCountByZone(AnalyzeDto analyzeDto);
}




