package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelLoopFault;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.LoopDto;
import com.scsdky.web.domain.vo.LoopCountVo;
import com.scsdky.web.domain.vo.LoopNumVo;

import java.util.List;

/**
 *
 */
public interface TunnelLoopFaultService extends IService<TunnelLoopFault> {

    /**
     * 回路故障
     * @param loopDto
     * @return
     */
    List<TunnelLoopFault> getLoopListByPage(LoopDto loopDto);

    /**
     * 统计个数
     * @param tunnelId
     * @return
     */
    LoopNumVo countByTunnelId(Integer tunnelId);

    List<LoopCountVo> getCountByZone(AnalyzeDto analyzeDto);
}
