package com.scsdky.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.TunnelStatistics;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DashboardPowerDto;
import com.scsdky.web.domain.dto.HomePageDto;
import com.scsdky.web.domain.dto.ModelDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import com.scsdky.web.domain.vo.statistics.CarbonVo;
import com.scsdky.web.domain.vo.dashboard.DashboardDeviceStatusVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPowerOverviewVo;
import com.scsdky.web.domain.vo.dashboard.DashboardLightUpReductionVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTodayPowerVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTunnelOverviewVo;
import com.scsdky.web.domain.vo.statistics.StatisticsVo;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * @author tubo
 */
public interface TunnelStatisticsService extends IService<TunnelStatistics> {

    /**
     * 统计分析
     * @param analyzeDto
     * @return
     */
    List<StatisticsVo> statistics(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 导出统计分析Excel
     * @param analyzeDto
     * @return
     */
    void exportStatistics(HttpServletResponse response, AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 能碳数据之碳排放量
     * @param analyzeDto
     * @return
     */
    List<CarbonVo> getCarbonByStatistics(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 电能
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
     * Dashboard 用户用电/节电概览（当前用户已分配隧道的年度+月度数据）
     * @param dto 含 year
     * @return 年度概览 + 月度明细
     */
    DashboardPowerOverviewVo userPowerOverview(DashboardPowerDto dto);

    /**
     * Dashboard 隧道概况（高速公路数、隧道总数、总里程）
     * @return 按当前用户权限过滤（admin 全量）
     */
    DashboardTunnelOverviewVo tunnelOverview();

    /**
     * Dashboard 设备状态分布（在线/离线/故障数量及占比）
     * @return 按当前用户权限过滤（admin 全量）
     */
    DashboardDeviceStatusVo deviceStatusDistribution();

    /**
     * Dashboard 今日数据汇总（今日用电量、今日节电量、今日碳减排）
     * @return 按当前用户权限过滤（admin 全量）
     */
    DashboardTodayPowerVo todayPowerSummary();

    /**
     * 本月理论亮灯时长削减率（用户管理所有隧道汇总）
     * 时间范围：本月 1 号 00:00 ~ 今日 23:59，有数据天数口径，总量汇总
     * @return 削减率（%）
     */
    DashboardLightUpReductionVo lightUpReductionRateCurrentMonth();

    /**
     * 模式切换
     * @param modelDto
     * @return
     * @throws JsonProcessingException
     */
    Boolean model(ModelDto modelDto) throws JsonProcessingException;

    /**
     * 获取当前工作模式
     * @param tunnelId
     * @return
     */
    TunnelEdgeComputingTerminal getCurrentModel(Long tunnelId);


    /**
     * 能碳数据统计
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo>  carbon(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 能碳数据统计2,测试定时任务用
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo>  carbon2(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 能碳数据v2版本
     * @param analyzeDto 分析参数
     * @return 能碳vo
     */
    List<EnergyCarbonVo> carbonV2(AnalyzeDto analyzeDto);

    /**
     * 构建电表数据
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo> carbonV3(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 构建电表数据
     * @param analyzeDto
     * @return
     */
    List<EnergyCarbonVo> carbonV4(AnalyzeDto analyzeDto) throws ParseException;
}
