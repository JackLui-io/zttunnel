package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelLightOutside;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.LightCountVo;
import com.scsdky.web.domain.vo.monitor.LightOutsideVo;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author tubo
 */
public interface TunnelLightOutsideService extends IService<TunnelLightOutside> {

    /**
     * 洞内外照度
     * @param analyzeDto
     * @return
     */
    List<InsideAndOutsideVo> insideAndOutside(AnalyzeDto analyzeDto) throws ParseException;
    /**
     * 导出洞内外照度Excel
     * @param analyzeDto
     * @return
     */
    void exportLightOutside(HttpServletResponse response, AnalyzeDto analyzeDto);

    /**
     * 每小时统计
     * @param analyzeDto
     * @return
     */
    List<LightOutsideVo> zdByHouse(AnalyzeDto analyzeDto);


    LightCountVo getAvgData(AnalyzeDto analyzeDto);

    /**
     * 查询v2版本
     * @param analyzeDto
     * @return
     */
    List<InsideAndOutsideVo> insideAndOutsideV2(AnalyzeDto analyzeDto) throws ParseException;
}
