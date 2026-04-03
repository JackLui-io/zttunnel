package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelInsideOutsideDay;
import com.scsdky.web.domain.vo.dashboard.TunnelLightingDaysVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 涂波
* @description 针对表【tunnel_inside_outside_day(车流车速每日数据)】的数据库操作Mapper
* @createDate 2025-04-09 17:00:39
* @Entity generator.domain.TunnelInsideOutsideDay
*/
public interface TunnelInsideOutsideDayMapper extends BaseMapper<TunnelInsideOutsideDay> {

    /**
     * 本月用户隧道亮灯时长总和（light_up 单位已是小时，与统计分析一致）
     */
    BigDecimal selectCurrentMonthLightUpSum(@Param("tunnelIds") List<Long> tunnelIds,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /**
     * 本月各隧道有亮灯数据的天数（与统计分析口径一致）
     */
    List<TunnelLightingDaysVo> selectCurrentMonthDataDaysByTunnel(@Param("tunnelIds") List<Long> tunnelIds,
                                                                 @Param("startDate") String startDate,
                                                                 @Param("endDate") String endDate);
}




