package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelTrafficFlow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.TrafficFlowCountVo;
import com.scsdky.web.domain.vo.TrafficFlowOrSpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficVo;
import com.scsdky.web.domain.vo.monitor.SpeedVo;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * @author tubo
 */
public interface TunnelTrafficFlowService extends IService<TunnelTrafficFlow> {

    /**
     * 统计每日车流 车速
     * @param analyzeDto
     * @return
     */
    List<TrafficFlowOrSpeedVo> trafficFlowOrSpeed(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 导出车流车速Excel
     * @param analyzeDto
     * @return
     */
    void exportTraffic(HttpServletResponse response, AnalyzeDto analyzeDto);

    /**
     * 统计车流量
     * @param analyzeDto
     * @return
     */
    List<TrafficVo> clByHouse(AnalyzeDto analyzeDto);

    /**
     * 每小时车速
     * @param analyzeDto
     * @return
     */
    List<SpeedVo> csByHouse(AnalyzeDto analyzeDto);

    TrafficFlowCountVo getAvgData(AnalyzeDto analyzeDto);

    /**
     * v2版本求车流车速
     * @param analyzeDto
     * @return
     */
    List<TrafficFlowOrSpeedVo>  trafficFlowOrSpeedV2(AnalyzeDto analyzeDto) ;
}
