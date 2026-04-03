package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelEnergyCarbon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 *
 */
public interface TunnelEnergyCarbonService extends IService<TunnelEnergyCarbon> {

    /**
     * 每日量统计
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo> carbon(AnalyzeDto analyzeDto);

    /**
     * 能碳数据导出
     * @param analyzeDto
     */
    void exportCarbon(HttpServletResponse response, AnalyzeDto analyzeDto);
}
