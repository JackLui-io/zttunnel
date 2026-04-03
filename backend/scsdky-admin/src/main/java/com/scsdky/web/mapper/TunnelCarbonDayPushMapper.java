package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelCarbonDayPush;
import com.scsdky.web.domain.PowerLightVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.AddrEnergyCarbonVo;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.dashboard.TunnelDataDaysByMonthVo;
import com.scsdky.web.domain.vo.dashboard.TunnelDataDaysVo;
import com.scsdky.web.domain.vo.dashboard.TunnelMonthlyConsumptionVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author 涂波
 * @description 针对表【tunnel_carbon_day_push(隧道能碳每日数据)】的数据库操作Mapper
 * @createDate 2025-06-23 10:52:20
 * @Entity generator.domain.TunnelCarbonDayPush
 */
public interface TunnelCarbonDayPushMapper extends BaseMapper<TunnelCarbonDayPush> {

    List<EnergyCarbonVo> getEnergyCarbonVo(@Param("devicelistIds") List<Long> devicelistIds, @Param("addrIds") List<Long> addrIds, @Param("analyzeDto") AnalyzeDto analyzeDto);

    List<AddrEnergyCarbonVo> getEnergyCarbonVo4(@Param("devicelistIds") List<Long> devicelistIds, @Param("addrIds") List<Long> addrIds, @Param("analyzeDto") AnalyzeDto analyzeDto);

    List<AddrEnergyCarbonVo> getLatestDataByAddrIds(@Param("devicelistIds") List<Long> devicelistIds, @Param("addrIds") List<Long> addrIds,@Param("analyzeDto")  AnalyzeDto analyzeDto);

    /**
     * Dashboard 用户月度用电：从 tunnel_carbon_day_push 按用户隧道汇总，仅指定年份 1-12 月
     */
    List<PowerLightVo> selectUserPowerByMonth(@Param("userId") Long userId, @Param("year") String year);

    /**
     * Dashboard admin 全量月度用电：不按用户过滤，汇总所有隧道 type=2 设备
     */
    List<PowerLightVo> selectAllPowerByMonth(@Param("year") String year);

    /**
     * 各隧道在 tunnel_carbon_day_push 中指定年份有电表数据的日期范围（type=2）
     * 仅返回有数据的隧道；无数据隧道不参与汇总
     */
    List<TunnelDataDaysVo> selectTunnelDataDaysRange(@Param("tunnelIds") List<Long> tunnelIds, @Param("year") String year);

    /**
     * 各隧道各月在 tunnel_carbon_day_push 中有数据的天数（type=2）
     */
    List<TunnelDataDaysByMonthVo> selectTunnelDataDaysByMonth(@Param("tunnelIds") List<Long> tunnelIds, @Param("year") String year);

    /**
     * Dashboard 今日用电：按 DATE(upload_time)=date 汇总，用户隧道过滤（Wh）
     */
    BigDecimal selectTodayPowerSumByUser(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Dashboard 今日用电：按 DATE(upload_time)=date 汇总，admin 全量（Wh）
     */
    BigDecimal selectTodayPowerSumAll(@Param("date") LocalDate date);

    /**
     * Dashboard 今日有数据的隧道 ID 列表（用户过滤，用于计算今日原设计）
     */
    List<Long> selectTunnelIdsWithDataOnDateByUser(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Dashboard 今日有数据的隧道 ID 列表（admin 全量，用于计算今日原设计）
     */
    List<Long> selectTunnelIdsWithDataOnDateAll(@Param("date") LocalDate date);

    /**
     * Dashboard 今日原设计功率汇总（Wh），用户过滤，tunnel_id 或 id 匹配 terminal
     */
    BigDecimal selectTodayDesignPowerSumByUser(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Dashboard 今日原设计功率汇总（Wh），admin 全量
     */
    BigDecimal selectTodayDesignPowerSumAll(@Param("date") LocalDate date);

    /**
     * Dashboard 今日实际耗电量（kWh）：批量版，与 getEnergyCarbonVo 的 LAG 逻辑一致，power_value 存 kWh。
     * 一次查询获取用户所有隧道的今日耗电，避免 N+1。
     *
     * @param userId 用户 ID，为 null 时查 admin 全量
     * @param date   目标日期（今日）
     * @return 今日耗电量（kWh），无数据时返回 0
     */
    BigDecimal selectTodayPowerConsumptionBatch(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 隧道月度耗电量 Top5（按耗电排序，power_value 存 kWh）
     *
     * @param userId 用户 ID，为 null 时查 admin 全量
     * @param year   年份
     * @return 最多 5 条
     */
    List<TunnelMonthlyConsumptionVo> selectTunnelMonthlyConsumptionTop5(@Param("userId") Long userId, @Param("year") String year);
}




