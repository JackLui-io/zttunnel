package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.HomePageDto;
import com.scsdky.web.domain.vo.DeviceTypeVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMonthlyRawVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import com.scsdky.web.domain.vo.statistics.CarbonVo;
import com.scsdky.web.domain.vo.statistics.StatisticsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TunnelStatistics
 */
public interface TunnelStatisticsMapper extends BaseMapper<TunnelStatistics> {

    /**
     * 统计分析
     * @param analyzeDto
     * @return
     */
    List<StatisticsVo> statistics(AnalyzeDto analyzeDto);

    /**
     *  能碳数据之碳排放量
     * @param analyzeDto
     * @return
     */
    List<CarbonVo> getCarbonByStatistics(AnalyzeDto analyzeDto);

    /**
     * 每小时电能参数
     * @param analyzeDto
     * @return
     */
    List<DnVo> dnByHouse(AnalyzeDto analyzeDto);

    /**
     * 首页--总用电、节电
     * @param homePageDto
     * @return
     */
    List<PowerLightVo> lightByMonth(HomePageDto homePageDto);

    /**
     * Dashboard 用户用电/节电月度数据（JOIN 用户隧道，仅 level-4，一次查询）
     * @param userId 当前用户ID
     * @param yearStart 年份开始 2026-01-01 00:00:00
     * @param yearEnd 年份结束 2027-01-01 00:00:00
     * @return 按月聚合的数据
     */
    List<DashboardMonthlyRawVo> selectUserPowerMonthly(@Param("userId") Long userId,
                                                       @Param("yearStart") String yearStart,
                                                       @Param("yearEnd") String yearEnd);

    /**
     * Dashboard 设备状态分布：多表聚合统计（边缘控制器、电能终端、洞外雷达、洞外亮度传感器、灯具终端）
     * 一次查询返回设备总数、在线、离线、故障数
     * @param tunnelIds 用户可见的 level-4 隧道 ID 列表（admin 为全部，非 admin 为扩展后的用户隧道）
     */
    DeviceTypeVo countDeviceStatusMultiTable(@Param("tunnelIds") List<Long> tunnelIds);
}




