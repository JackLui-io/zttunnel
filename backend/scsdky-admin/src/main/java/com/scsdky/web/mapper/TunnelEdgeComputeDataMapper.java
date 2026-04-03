package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.dashboard.MonthlyLightingVo;
import com.scsdky.web.domain.vo.dashboard.TunnelLightingDaysVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity generator.domain.TunnelEdgeComputeData
 */
public interface TunnelEdgeComputeDataMapper extends BaseMapper<TunnelEdgeComputeData> {

    List<Map<String,Object>> getLightTime(AnalyzeDto analyzeDto);

    /**
     * Dashboard 按月汇总亮灯时长（lighting_time 单位：0.1h，×0.1 得小时）
     *
     * @param tunnelIds 隧道 ID 列表，type=1 边缘计算设备
     * @param year      年份
     * @return 各月 month、totalLightingTime（×0.1 为实际亮灯小时）
     */
    List<MonthlyLightingVo> selectMonthlyLightingTimeSum(@Param("tunnelIds") List<Long> tunnelIds, @Param("year") String year);

    /**
     * 本月 1 号 00:00 ~ endTime 23:59 内，用户隧道亮灯时长总和
     *
     * @param tunnelIds 隧道 ID 列表
     * @param startTime 开始时间（本月 1 号 00:00:00）
     * @param endTime   结束时间（今日 23:59:59）
     * @return lighting_time 原始汇总（×0.1 为实际亮灯小时）
     */
    Long selectCurrentMonthLightingTimeSum(@Param("tunnelIds") List<Long> tunnelIds,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);

    /**
     * 本月各隧道有亮灯数据的天数
     *
     * @param tunnelIds 隧道 ID 列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 各隧道 tunnelId、daysInMonth
     */
    List<TunnelLightingDaysVo> selectCurrentMonthDataDaysByTunnel(@Param("tunnelIds") List<Long> tunnelIds,
                                                                  @Param("startTime") String startTime,
                                                                  @Param("endTime") String endTime);

    /**
     * 批量查询边缘计算终端的最新记录（按id倒序，每个设备一条）
     * 
     * @param deviceIds 设备ID列表
     * @return 每个设备的最新记录（devicelist_id, upload_time）
     */
    List<TunnelEdgeComputeData> selectLatestByDeviceIds(@Param("deviceIds") List<Long> deviceIds);
}




