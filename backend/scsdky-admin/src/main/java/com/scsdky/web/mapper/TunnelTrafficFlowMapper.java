package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelTrafficFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.TrafficFlowCountVo;
import com.scsdky.web.domain.vo.TrafficFlowOrSpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficSpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficVo;
import com.scsdky.web.domain.vo.monitor.SpeedVo;

import java.util.List;

/**
 * @Entity generator.domain.TunnelTrafficFlow
 */
public interface TunnelTrafficFlowMapper extends BaseMapper<TunnelTrafficFlow> {

    /**
     * 统计每日车流、车速
     * @param analyzeDto
     * @return
     */
    List<TrafficFlowOrSpeedVo> trafficFlowOrSpeed(AnalyzeDto analyzeDto);

    /**
     * 每小时车流量
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
     * 获取每小时车流量
     * @param analyzeDto
     * @return
     */
    List<TrafficVo> getHourTraffic(AnalyzeDto analyzeDto);

    /**
     * 根据查询获取所有车速数据
     * @param analyzeDto
     * @return
     */
    List<TrafficSpeedVo> getAllSpeedByQuery(AnalyzeDto analyzeDto);

}




