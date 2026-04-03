package com.scsdky.web.mapper;

import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.TunnelPowerData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import io.swagger.annotations.Api;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TunnelPowerData
 */
public interface TunnelPowerDataMapper extends BaseMapper<TunnelPowerData> {

    /**
     * 每小时电表耗电量
     * @param ascUniqueIds 电表id
     * @param descUniqueIds 电表id
     * @param nowDate
     * @return
     */
    List<DnVo> getPowerDataValue(@Param("ascUniqueIds") List<Long> ascUniqueIds,
                                 @Param("descUniqueIds") List<Long> descUniqueIds,
                                 @Param("nowDate")String nowDate);

    List<EnergyCarbonVo> selectCountValue(@Param("ascUniqueIds") List<Long> ascUniqueIds,
                                          @Param("analyzeDto") AnalyzeDto analyzeDto);

    List<PowerLightVo> selectImepGroupByMonth(@Param("year") String year,
                                              @Param("ascUniqueIds") List<Long> ascUniqueIds);

    /**
     * Dashboard 用户月度用电（一次 SQL，JOIN 用户隧道+电能终端+电表，仅返回指定年份 1-12 月）
     */
    List<PowerLightVo> selectUserPowerByMonth(@Param("userId") Long userId, @Param("year") String year);
}




