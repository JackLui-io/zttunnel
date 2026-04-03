package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelPowerData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.monitor.DnVo;

import java.util.List;

/**
 * @author tubo
 */
public interface TunnelPowerDataService extends IService<TunnelPowerData> {

    /**
     * 统计电表小时的耗电量
     * @param ascUniqueIds 升序的电表值
     * @param descUniqueIds 降序的电表值
     * @param nowDate
     * @return
     */
    List<DnVo> getPowerDataValue(List<Long> ascUniqueIds, List<Long> descUniqueIds,String nowDate);

    /**
     * 计算能碳
     * @param ascUniqueIds
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo> selectCountValue(List<Long> ascUniqueIds, AnalyzeDto analyzeDto);

    /**
     * 按照月份统计电表读数
     * @param year
     * @param ascUniqueIds
     */
    List<PowerLightVo> selectImepGroupByMonth(String year, List<Long> ascUniqueIds);
}
