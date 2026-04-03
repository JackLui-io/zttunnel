package com.scsdky.web.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DashboardPowerDto;
import com.scsdky.web.domain.dto.HomePageDto;
import com.scsdky.web.domain.dto.ModelDto;
import com.scsdky.web.domain.vo.AddrEnergyCarbonVo;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.MeterReadingVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import com.scsdky.web.domain.vo.statistics.CarbonVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMonthlyRawVo;
import com.scsdky.web.domain.vo.dashboard.DashboardDeviceStatusVo;
import com.scsdky.web.domain.vo.dashboard.DashboardLightUpReductionVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPowerOverviewVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTodayPowerVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTunnelOverviewVo;
import com.scsdky.web.domain.vo.dashboard.TunnelDataDaysByMonthVo;
import com.scsdky.web.domain.vo.dashboard.TunnelDataDaysVo;
import com.scsdky.web.domain.vo.dashboard.TunnelLightingDaysVo;
import com.scsdky.web.domain.vo.statistics.StatisticsExcelVo;
import com.scsdky.web.domain.vo.statistics.StatisticsVo;
import com.scsdky.web.mapper.TunnelCarbonDayPushMapper;
import com.scsdky.web.mapper.TunnelDevicelistMapper;
import com.scsdky.web.mapper.TunnelEdgeComputeDataMapper;
import com.scsdky.web.mapper.TunnelInsideOutsideDayMapper;
import com.scsdky.web.mapper.TunnelPowerDataMapper;
import com.scsdky.web.mapper.TunnelStatisticsMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.DateUtils;
import com.scsdky.web.utils.ExportExcelUtils;
import javassist.runtime.Desc;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelStatisticsServiceImpl extends ServiceImpl<TunnelStatisticsMapper, TunnelStatistics> implements TunnelStatisticsService{

    @Resource
    private UserTunnelService userTunnelService;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelSyscmdService tunnelSyscmdService;

    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelPowerDataService tunnelPowerDataService;

    @Resource
    private TunnelPowerDataMapper tunnelPowerDataMapper;

    @Resource
    private TunnelCarbonDayService tunnelCarbonDayService;

    @Resource
    private TunnelCarbonDayPushService tunnelCarbonDayPushService;

    @Resource
    private TunnelCarbonDayPushMapper tunnelCarbonDayPushMapper;

    @Resource
    private TunnelEdgeComputeDataMapper tunnelEdgeComputeDataMapper;

    @Resource
    private TunnelInsideOutsideDayMapper tunnelInsideOutsideDayMapper;

    @Resource
    private TunnelDeviceService tunnelDeviceService;

    @Resource
    private TunnelDevicelistMapper tunnelDevicelistMapper;

    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;

    @Resource
    private  TunnelLightOutsideService tunnelLightOutsideService;

    private static final DecimalFormat FORMAT = new DecimalFormat("#.0");
    /** power_value(Wh) 转万kWh：tunnel_carbon_day_push 电表读数为 Wh，1万kWh=10^7 Wh */
    private static final BigDecimal WH_TO_WAN_KWH = BigDecimal.valueOf(10000000);
    /** kWh 转万kWh */
    private static final BigDecimal KWH_TO_WAN_KWH = BigDecimal.valueOf(10000);

    @Override
    public List<StatisticsVo> statistics(AnalyzeDto analyzeDto) throws ParseException {


        List<Long> tunnelIds = new ArrayList<>();
        if(analyzeDto.getTunnelId() == null ){
            Long userId = SecurityUtils.getUserId();
            List<UserTunnel> userTunnels = userTunnelService.list(new LambdaQueryWrapper<UserTunnel>().eq(UserTunnel::getUserId, userId));
            tunnelIds = userTunnels.stream().map(UserTunnel::getTunnelNameId).collect(Collectors.toList());
        }else{
            tunnelIds.add(analyzeDto.getTunnelId());
        }
        analyzeDto.setTunnelIds(tunnelIds);
        //根据隧道id 查询编号
        TunnelNameResult tunnelNameResult = tunnelNameResultService.getById(analyzeDto.getTunnelId());
        //通过隧道id获取边缘控制器的id--后续抽成公用方法，这些太臃肿了
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelNameResult.getId());
        //原设计耗电量--通过选择的时间去计算需要好多耗电量 24是每天有24个小时
        int day = 0;
        if(StringUtils.isNotBlank(analyzeDto.getYear())) {
            //查询今年的最早开始时间和结束日期
            TunnelEdgeComputeData startTime = tunnelEdgeComputeDataService.getOne(new LambdaQueryWrapper<TunnelEdgeComputeData>()
                    .gt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-01-01")
                    .lt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-12-31 23:59:59")
                    .eq(TunnelEdgeComputeData::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                    .orderByAsc(TunnelEdgeComputeData::getUploadTime)
                    .last("limit 1"));

            //最晚时间
            TunnelEdgeComputeData endTime = tunnelEdgeComputeDataService.getOne(new LambdaQueryWrapper<TunnelEdgeComputeData>()
                    .lt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-12-31 23:59:59")
                    .eq(TunnelEdgeComputeData::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                    .orderByDesc(TunnelEdgeComputeData::getUploadTime)
                    .last("limit 1"));
            analyzeDto.setStartTime(startTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(startTime.getUploadTime()));
            analyzeDto.setEndTime(endTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(endTime.getUploadTime()));
            day = DateUtils.getDay(startTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(startTime.getUploadTime()), endTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(endTime.getUploadTime()));

        }

        //统计分析模块
        if(StringUtils.isNotBlank(analyzeDto.getStartTime()) && StringUtils.isNotBlank(analyzeDto.getEndTime())) {
            //统计分析模块按年月日统计
            day = DateUtils.getDay(analyzeDto.getEndTime(), analyzeDto.getStartTime());
        }
        //原设计耗电量
        int originalPowerConsumption = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal() * (day == 0 ? 1 : day) * 24;
        //原设计碳排放量`
        BigDecimal originalCarbonEmission = new BigDecimal(String.valueOf(originalPowerConsumption)).multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN);
        //原设计运行功率
        int originalOperatingPower = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
        //原单位里程耗电量
        BigDecimal originalUnitPowerConsumption = new BigDecimal(originalPowerConsumption).divide(tunnelNameResult.getTunnelMileage(),2, RoundingMode.FLOOR);
        //原设计亮灯时间
        int originalLightUpTime = (day == 0 ? 1 : day) * 24 ;
        List<StatisticsVo> result = new ArrayList<>();
        StatisticsVo statisticsVo = new StatisticsVo();
        statisticsVo.setOriginalPowerConsumption(originalPowerConsumption);
        statisticsVo.setOriginalCarbonEmission(originalCarbonEmission);
        statisticsVo.setOriginalOperatingPower(originalOperatingPower);
        statisticsVo.setOriginalLightUpTime(originalLightUpTime);

        //实际耗电量 先写死，电表数据获取到，直接替换,要用当前时间的耗电量--开始时间前的耗电量
        double actualPower;

        List<EnergyCarbonVo> energyCarbonVos = carbonV3(analyzeDto);
        actualPower = energyCarbonVos.stream().mapToDouble(energyCarbonVo -> Optional.ofNullable(energyCarbonVo.getDailyPowerConsumption()).orElse(0.0)).sum();

        statisticsVo.setActualPowerConsumption(new BigDecimal(actualPower).setScale(2,RoundingMode.DOWN).doubleValue());
        //实际单位里程耗电量
        statisticsVo.setActualUnitPowerConsumption(new BigDecimal(actualPower).divide(tunnelNameResult.getTunnelMileage(),2, RoundingMode.FLOOR).intValue());
        //实际碳排放量
        statisticsVo.setActualCarbonEmission(new BigDecimal(String.valueOf(actualPower)).multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN));
        //实际亮灯时间--通过上传数据表tunnel_edge_compute_data表中去获取实际亮灯时间
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId() );

        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutsideV2(analyzeDto);
        double actualLightUpTime = insideAndOutsideVos.stream().mapToDouble(insideAndOutsideVo -> Double.parseDouble(insideAndOutsideVo.getLightUp())).sum();

        //亮灯时长--小时计算
        BigDecimal hours = new BigDecimal(FORMAT.format(actualLightUpTime));
        //实际运行功率就是平均功率，使用实际耗电量/实际亮灯时间
        if(hours.intValue() == 0) {
            statisticsVo.setActualOperatingPower(BigDecimal.ZERO);
        }else{
            statisticsVo.setActualOperatingPower(new BigDecimal(String.valueOf(actualPower)).divide(BigDecimal.valueOf(originalLightUpTime), 2, RoundingMode.DOWN));        }
        //实际亮灯时长
        statisticsVo.setActualLightUpTime(hours);
        //理论节电率
        double v2 = (originalPowerConsumption * 1.0 - statisticsVo.getActualPowerConsumption()) / originalPowerConsumption;
        statisticsVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论碳减排率
        BigDecimal subtract = originalCarbonEmission.subtract(statisticsVo.getActualCarbonEmission());
        BigDecimal theoreticalCarbonEmissionReduction = subtract.divide(originalCarbonEmission, 2, RoundingMode.HALF_UP);
        statisticsVo.setTheoreticalCarbonEmissionReduction(theoreticalCarbonEmissionReduction.multiply(BigDecimal.valueOf(100)));
        //理论总功率消减
        double v3 = (originalOperatingPower * 1.0 - statisticsVo.getActualOperatingPower().doubleValue()) / originalOperatingPower;
        statisticsVo.setTheoreticalTotalPowerReduction(new BigDecimal(v3).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论亮灯时间削减
        double v = (statisticsVo.getOriginalLightUpTime() * 1.0 - statisticsVo.getActualLightUpTime().doubleValue()) / statisticsVo.getOriginalLightUpTime();
        statisticsVo.setTheoreticalLightUpTimeReduction(new BigDecimal(v).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论削减运行功率
        double v1 = (originalUnitPowerConsumption.intValue() * 1.0 - statisticsVo.getActualUnitPowerConsumption()) / originalUnitPowerConsumption.intValue();
        //statisticsVo.setTheoreticalOperatingPowerReduction(new BigDecimal(v1).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        statisticsVo.setTheoreticalOperatingPowerReduction(BigDecimal.valueOf(tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal()).subtract(statisticsVo.getActualOperatingPower()).setScale(2,RoundingMode.UP));
        result.add(statisticsVo);
        return result;
    }

    private double getActualPower(AnalyzeDto analyzeDto, double actualPower, List<Long> ascUniqueIds) throws ParseException {
        if(CollectionUtils.isNotEmpty(ascUniqueIds)) {
            double endNum;
            double startNum;
            //取累计消耗电量最大的
            QueryWrapper<TunnelPowerData> tunnelPowerDataQueryWrapper = new QueryWrapper<>();
            tunnelPowerDataQueryWrapper.in("unique_id",ascUniqueIds);
            //时间非空
            if(StringUtils.isNotBlank(analyzeDto.getStartTime()) && StringUtils.isNotBlank(analyzeDto.getEndTime())) {
                tunnelPowerDataQueryWrapper.gt("upload_time",analyzeDto.getStartTime());
                tunnelPowerDataQueryWrapper.le("upload_time",analyzeDto.getEndTime() + " 23:59:59");
            }
            tunnelPowerDataQueryWrapper.eq("name","ImpEp");
            tunnelPowerDataQueryWrapper.select("Max(value) value ");
            tunnelPowerDataQueryWrapper.groupBy("unique_id");

            List<Map<String, Object>> powerDataListEnd = tunnelPowerDataService.listMaps(tunnelPowerDataQueryWrapper);

            endNum = powerDataListEnd.stream().mapToDouble(map -> Double.parseDouble(String.valueOf(map.get("value")))).sum();


            //取开始时间的耗电量，用当前时的耗电量--开始的耗电量， 等于累计的耗电量
            tunnelPowerDataQueryWrapper = new QueryWrapper<>();
            tunnelPowerDataQueryWrapper.in("unique_id",ascUniqueIds);
            //时间非空
            if(StringUtils.isNotBlank(analyzeDto.getStartTime())){
                tunnelPowerDataQueryWrapper.le("upload_time",DateUtils.getDay(-1,analyzeDto.getStartTime()) + " 23:59:59");
            }
            tunnelPowerDataQueryWrapper.eq("name","ImpEp");
            tunnelPowerDataQueryWrapper.select("Max(value) value ");
            tunnelPowerDataQueryWrapper.groupBy("unique_id");
            List<Map<String, Object>> powerDataListStart = tunnelPowerDataService.listMaps(tunnelPowerDataQueryWrapper);
            startNum = powerDataListStart.stream().mapToDouble(map -> Double.parseDouble(String.valueOf(map.get("value")))).sum();
            //如果最新的一天没有耗电量，直接用开始时间的耗电量
            if(endNum == 0) {
                actualPower = startNum;
            }else{
                actualPower = endNum - startNum;
            }
        }
        return actualPower;
    }

    private double getStartNum(double startNum, LambdaQueryWrapper<TunnelPowerData> tunnelPowerDataLambdaQueryWrapper) {
        tunnelPowerDataLambdaQueryWrapper.orderByDesc(TunnelPowerData::getUploadTime);
        tunnelPowerDataLambdaQueryWrapper.last("limit 1");
        TunnelPowerData powerData = tunnelPowerDataService.getOne(tunnelPowerDataLambdaQueryWrapper);
        if(powerData != null ) {
            startNum += powerData.getValue().doubleValue();
        }
        return startNum;
    }

    @Override
    public void exportStatistics(HttpServletResponse response, AnalyzeDto analyzeDto) throws ParseException {
        List<Long> tunnelIds = new ArrayList<>();
        if(analyzeDto.getTunnelId() == null ){
            Long userId = SecurityUtils.getUserId();
            List<UserTunnel> userTunnels = userTunnelService.list(new LambdaQueryWrapper<UserTunnel>().eq(UserTunnel::getUserId, userId));
            tunnelIds = userTunnels.stream().map(UserTunnel::getTunnelNameId).collect(Collectors.toList());
        }else{
            tunnelIds.add(analyzeDto.getTunnelId());
        }
        analyzeDto.setTunnelIds(tunnelIds);
        List<StatisticsVo> statisticsVos = baseMapper.statistics(analyzeDto);
        statisticsVos.removeAll(Collections.singleton(null));
        //根据隧道id 查询编号
        TunnelNameResult tunnelNameResult = tunnelNameResultService.getById(analyzeDto.getTunnelId());
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelNameResult.getId());
        // 获取隧道名称
        TunnelNameResult tunnelParent = tunnelNameResultService.getById(tunnelNameResult.getParentId());
        // 获取高速公路名称
        TunnelNameResult tunnelRoadParent = tunnelNameResultService.getById(tunnelParent.getParentId());
        StatisticsExcelVo statisticsExcelVo = new StatisticsExcelVo();
        statisticsExcelVo.setTitle(tunnelRoadParent.getTunnelName()+tunnelParent.getTunnelName()+"("
                +tunnelNameResult.getTunnelName()+")");
        statisticsExcelVo.setStartDate(analyzeDto.getStartTime());
        statisticsExcelVo.setEndDate(analyzeDto.getEndTime());

        //实际亮灯时间--通过上传数据表tunnel_edge_compute_data表中去获取实际亮灯时间
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        //原设计耗电量--通过选择的时间去计算需要好多耗电量 24是每天有24个小时
        int day = 0;
        if(StringUtils.isNotBlank(analyzeDto.getYear())) {
            //查询今年的最早开始时间和结束日期
            TunnelEdgeComputeData startTime = tunnelEdgeComputeDataService.getOne(new LambdaQueryWrapper<TunnelEdgeComputeData>()
                    .gt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-01-01")
                    .lt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-12-31 23:59:59")
                    .eq(TunnelEdgeComputeData::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                    .orderByAsc(TunnelEdgeComputeData::getUploadTime)
                    .last("limit 1"));

            //最晚时间
            TunnelEdgeComputeData endTime = tunnelEdgeComputeDataService.getOne(new LambdaQueryWrapper<TunnelEdgeComputeData>()
                    .lt(TunnelEdgeComputeData::getUploadTime, analyzeDto.getYear() + "-12-31 23:59:59")
                    .eq(TunnelEdgeComputeData::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                    .orderByDesc(TunnelEdgeComputeData::getUploadTime)
                    .last("limit 1"));
            analyzeDto.setStartTime(startTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(startTime.getUploadTime()));
            analyzeDto.setEndTime(endTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(endTime.getUploadTime()));
            day = DateUtils.getDay(startTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(startTime.getUploadTime()), endTime==null ? analyzeDto.getYear() + "-01-01" : DateUtils.dateTime(endTime.getUploadTime()));

        }

        //统计分析模块
        if(StringUtils.isNotBlank(analyzeDto.getStartTime()) && StringUtils.isNotBlank(analyzeDto.getEndTime())) {
            //统计分析模块按年月日统计
            day = DateUtils.getDay(analyzeDto.getEndTime(), analyzeDto.getStartTime());
        }

        int originalPowerConsumption = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal() * (day == 0 ? 1 : day) * 24;
        statisticsExcelVo.setOriginalPowerConsumption(originalPowerConsumption);
        //原设计碳排放量
        BigDecimal originalCarbonEmission = new BigDecimal(String.valueOf(originalPowerConsumption)).multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN);
        statisticsExcelVo.setOriginalCarbonEmission(originalCarbonEmission);
        //原设计运行功率
        int originalOperatingPower = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
        statisticsExcelVo.setOriginalOperatingPower(String.valueOf(originalOperatingPower));
        //原设计亮灯时间
        int originalLightUpTime = (day == 0 ? 1 : day) * 24 ;
        statisticsExcelVo.setOriginalPowerConsumption(originalPowerConsumption);
        statisticsExcelVo.setOriginalCarbonEmission(originalCarbonEmission);
        statisticsExcelVo.setOriginalLightUpTime(originalLightUpTime);

        //实际耗电量 先写死，电表数据获取到，直接替换,要用当前时间的耗电量--开始时间前的耗电量
        double actualPower;

        List<EnergyCarbonVo> energyCarbonVos = carbonV3(analyzeDto);
        actualPower = energyCarbonVos.stream().mapToDouble(energyCarbonVo -> Optional.ofNullable(energyCarbonVo.getDailyPowerConsumption()).orElse(0.0)).sum();
        statisticsExcelVo.setActualPowerConsumption(new BigDecimal(actualPower).setScale(2,RoundingMode.DOWN).doubleValue());

        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId() );

        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutsideV2(analyzeDto);
        double actualLightUpTime = insideAndOutsideVos.stream().mapToDouble(insideAndOutsideVo -> Double.parseDouble(insideAndOutsideVo.getLightUp())).sum();
        //亮灯时长--小时计算
        BigDecimal hours = new BigDecimal(FORMAT.format(actualLightUpTime));
        statisticsExcelVo.setActualLightUpTime(hours);
        //实际运行功率就是平均功率，使用实际耗电量/实际亮灯时间
        if(hours.intValue() == 0) {
            statisticsExcelVo.setActualOperatingPower(BigDecimal.ZERO);
        }else{
            statisticsExcelVo.setActualOperatingPower(new BigDecimal(String.valueOf(actualPower)).divide(BigDecimal.valueOf(originalLightUpTime), 2, RoundingMode.DOWN));        }
        //实际碳排放量
        statisticsExcelVo.setActualCarbonEmission(new BigDecimal(String.valueOf(actualPower)).multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN));

        //理论节电率
        double v2 = (originalPowerConsumption * 1.0 - statisticsExcelVo.getActualPowerConsumption()) / originalPowerConsumption;
        statisticsExcelVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论节电量 = 原设计耗电量 - 实际耗电量
        statisticsExcelVo.setTheoreticalPowerSaving(new BigDecimal(originalPowerConsumption).subtract(new BigDecimal(actualPower).setScale(2,RoundingMode.DOWN)));
        //理论碳减排率
        BigDecimal subtract = originalCarbonEmission.subtract(statisticsExcelVo.getActualCarbonEmission());
        BigDecimal theoreticalCarbonEmissionReduction = subtract.divide(originalCarbonEmission, 2, RoundingMode.HALF_UP);
        //statisticsExcelVo.setTheoreticalCarbonEmissionReduction(theoreticalCarbonEmissionReduction.multiply(BigDecimal.valueOf(100)));
        //理论碳减排量 == 原设计碳排放量 - 实际碳排放量
        statisticsExcelVo.setTheoreticalCarbonEmissionReduction(originalCarbonEmission.subtract(statisticsExcelVo.getActualCarbonEmission()));
        //理论碳减排率
        statisticsExcelVo.setTheoreticalCarbonEmissionReductionRate(theoreticalCarbonEmissionReduction.multiply(BigDecimal.valueOf(100)));


        //理论总功率消减
        double v3 = (originalOperatingPower * 1.0 - statisticsExcelVo.getActualOperatingPower().doubleValue()) / originalOperatingPower;
        statisticsExcelVo.setTheoreticalTotalPowerReduction(new BigDecimal(v3).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论亮灯时间削减
        double v = (statisticsExcelVo.getOriginalLightUpTime() * 1.0 - statisticsExcelVo.getActualLightUpTime().doubleValue()) / statisticsExcelVo.getOriginalLightUpTime();
        //理论亮灯时间消减 == 原设计亮灯时长 - 实际亮灯时长
        statisticsExcelVo.setTheoreticalLightUpTimeReduction(new BigDecimal(originalLightUpTime).subtract(statisticsExcelVo.getActualLightUpTime()).setScale(2,RoundingMode.UP));
        //理论削减运行功率
        statisticsExcelVo.setTheoreticalLightUpTimeReductionRate(new BigDecimal(v).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.UP));
        //理论消减功率 == 原设计运行功率 - 实际运行功率
        statisticsExcelVo.setTheoreticalOperatingPowerReduction(new BigDecimal(originalOperatingPower).subtract(statisticsExcelVo.getActualOperatingPower()).setScale(2,RoundingMode.UP));

        try {
            ExportExcelUtils.statisticsExportTemp(response, statisticsExcelVo);
        }catch (Exception e){
            throw new ServiceException("导出总体分析失败！");
        }
    }

    @Override
    public List<CarbonVo> getCarbonByStatistics(AnalyzeDto analyzeDto) throws ParseException {
        List<CarbonVo> result = new ArrayList<>();
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //实际耗电量
        double actualPower;
        //获取所有电表地址
        /**List<Long> ascUniqueIds = getAllUniqueIdOrAddrId(analyzeDto,tunnelEdgeComputingTerminal,1);
         actualPower = getActualPower(analyzeDto, actualPower, ascUniqueIds);**/
        List<EnergyCarbonVo> energyCarbonVos = carbonV4(analyzeDto);
        actualPower = energyCarbonVos.stream().mapToDouble(energyCarbonVo -> Optional.ofNullable(energyCarbonVo.getDailyPowerConsumption()).orElse(0.0)).sum();

        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutsideV2(analyzeDto);


        for (InsideAndOutsideVo insideAndOutsideVo : insideAndOutsideVos) {
            CarbonVo carbonVo = new CarbonVo();
            //回路实际运行功率 *  24 * 太排放因子 / 1000
            BigDecimal actualCarbonPower = new BigDecimal(String.valueOf(actualPower)).divide(BigDecimal.valueOf(new BigDecimal(insideAndOutsideVo.getLightUp()).doubleValue() == 0.0 ? 1.0 : new BigDecimal(insideAndOutsideVo.getLightUp()).doubleValue()), 2, RoundingMode.DOWN);
            //实际碳排放量
            carbonVo.setActualCarbonEmission(actualCarbonPower.multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN));
            carbonVo.setUploadTime(insideAndOutsideVo.getUploadTime());
            int originalPowerConsumptionDay = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal() * 24;
            BigDecimal subtract = new BigDecimal(String.valueOf(originalPowerConsumptionDay)).subtract(actualCarbonPower);
            //原设计碳排放量
            BigDecimal originalCarbonEmissionDay = subtract.multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor()).divide(new BigDecimal("1000"),2, RoundingMode.DOWN);
            carbonVo.setOriginalCarbonEmission(originalCarbonEmissionDay);
            result.add(carbonVo);
        }

        return result;
    }

    @Override
    public List<DnVo> dnByHouse(AnalyzeDto analyzeDto) {
        //获取当天的年月日
        String nowDate = DateUtils.getDate();

        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //获取所有电表地址
        List<Long> allUniqueId = getAllUniqueIdOrAddrId(analyzeDto,tunnelEdgeComputingTerminal,1);

        if(CollectionUtils.isNotEmpty(allUniqueId)) {
            List<DnVo> hourValues = tunnelPowerDataService.getPowerDataValue(allUniqueId,null,nowDate);
            //总回路的设计功率
            Integer designOperatingPowerTotal = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
            //计算理论节电量
            return getDnVos(designOperatingPowerTotal, hourValues);
        }

        return new ArrayList<>();
    }


    /**
     * 获取所有的电表地址
     * @param analyzeDto
     * @param tunnelEdgeComputingTerminal
     * @param type 1 查询电表唯一id，2 查询地址号
     * @return
     */
    public List<Long> getAllUniqueIdOrAddrId(AnalyzeDto analyzeDto,TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal,int type){

        //查询当前隧道下有几个电能终端
        List<TunnelDevicelistTunnelinfo> tunnelDevicelistTunnelinfos = tunnelDevicelistTunnelinfoService
                .list(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                        .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                        .eq(TunnelDevicelistTunnelinfo::getType,2));


        //存储所有的电表地址
        List<Long> allUniqueId = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(tunnelDevicelistTunnelinfos)) {
            if(tunnelEdgeComputingTerminal.getDirection().contains("右线")) {
                List<Long> allUniqueIdsR = new ArrayList<>();
                //通过编号查询右线对应的电表地址
                allUniqueId = getAllUniqueIdOrAddrId(tunnelDevicelistTunnelinfos, 1, allUniqueIdsR,type);
            }
            if(tunnelEdgeComputingTerminal.getDirection().contains("左线")) {
                List<Long> allUniqueIdsL = new ArrayList<>();
                //通过编号查询右线对应的电表地址
                allUniqueId = getAllUniqueIdOrAddrId(tunnelDevicelistTunnelinfos, 2, allUniqueIdsL,type);
            }
        }
        return allUniqueId;
    }

    private List<DnVo> getDnVos(Integer designOperatingPowerTotal, List<DnVo> hourValues) {
        hourValues.forEach(dnVo -> {
            BigDecimal theoreticalPowerSavings = new BigDecimal(designOperatingPowerTotal).subtract(new BigDecimal(dnVo.getPowerConsumption()));
            dnVo.setTheoreticalPowerSavings(String.valueOf(theoreticalPowerSavings.setScale(2, RoundingMode.DOWN)));
            dnVo.setPowerConsumption(dnVo.getPowerConsumption());
        });
        return hourValues;
    }

    /**
     * 获取电能终端下的电表id
     * @param delay
     * @param sortType
     * @param devicelistId
     * @return
     */
    private List<Long> getTunnelPowerEdgeComputing(Integer delay, String sortType,Long devicelistId) {
        LambdaQueryWrapper<TunnelPowerEdgeComputing> tunnelPowerEdgeComputingLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tunnelPowerEdgeComputingLambdaQueryWrapper.select(TunnelPowerEdgeComputing::getUniqueId);
        tunnelPowerEdgeComputingLambdaQueryWrapper.eq(TunnelPowerEdgeComputing::getDevicelistId, devicelistId);
        tunnelPowerEdgeComputingLambdaQueryWrapper.last("order by meter_index "  + sortType + " limit " + delay);
        List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.list(tunnelPowerEdgeComputingLambdaQueryWrapper);
        List<Long> uniqueIds = tunnelPowerEdgeComputings.stream().map(TunnelPowerEdgeComputing::getUniqueId).collect(Collectors.toList());
        return uniqueIds;
    }

    @Override
    public List<PowerLightVo> lightByMonth(HomePageDto homePageDto) {
        List<Long> tunnelIds = new ArrayList<>();
        if(homePageDto.getTunnelId() == null ){
            Long userId = SecurityUtils.getUserId();
            List<UserTunnel> userTunnels = userTunnelService.list(new LambdaQueryWrapper<UserTunnel>().eq(UserTunnel::getUserId, userId));
            tunnelIds = userTunnels.stream().map(UserTunnel::getTunnelNameId).collect(Collectors.toList());
        }else{
            tunnelIds.add(homePageDto.getTunnelId());
        }
        homePageDto.setTunnelIds(tunnelIds);
        // 老版本
        // List<PowerLightVo> useLightVos = baseMapper.lightByMonth(homePageDto);
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(homePageDto.getTunnelId());

        /*if(tunnelDevicelistTunnelinfos.size() == 1 ){
            //通过电能终端id，获取需要查询的电表id,第一个为升序取值，如果只有一个电能终端，则取当前电能终端下的所有电表
            ascUniqueIds = getTunnelPowerEdgeComputing(999, "asc", tunnelDevicelistTunnelinfos.get(0).getDevicelistId());
        }
        //有两个电能终端，取delay的电表数来计算，如果是开头，升序取，第二个倒序取
        if(tunnelDevicelistTunnelinfos.size() == 2 ){
            //通过电能终端id，获取需要查询的电表id,第一个为升序取值
            ascUniqueIds = getTunnelPowerEdgeComputing(delay / 2,tunnelEdgeComputingTerminal.getDirection().contains("右线") ? "asc":"desc",tunnelDevicelistTunnelinfos.get(0).getDevicelistId());
            //求第二个电能终端中的电表数据,第二个为降序取值
            List<Long> descUniqueIds = getTunnelPowerEdgeComputing(delay / 2, tunnelEdgeComputingTerminal.getDirection().contains("右线") ? "desc":"asc", tunnelDevicelistTunnelinfos.get(1).getDevicelistId());
            ascUniqueIds.addAll(descUniqueIds);
        }*/
        AnalyzeDto analyzeDto = new AnalyzeDto();
        analyzeDto.setTunnelId(homePageDto.getTunnelId());
        List<Long> ascUniqueIds = getAllUniqueIdOrAddrId(analyzeDto,tunnelEdgeComputingTerminal,1);
        if(CollectionUtils.isNotEmpty(ascUniqueIds)) {
            //电表的每个月的实际耗电量
            List<PowerLightVo> powerLightVos = tunnelPowerDataService.selectImepGroupByMonth(homePageDto.getYear(),ascUniqueIds);

            powerLightVos.forEach(powerLightVo -> {
                //每个月的耗电天数
                int day = DateUtils.getDaysInMonth(Integer.parseInt(homePageDto.getYear()),powerLightVo.getMonth());
                int originalPowerConsumption = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal() * day * 24;
                powerLightVo.setTotalEconomyLight(new BigDecimal(originalPowerConsumption).subtract(powerLightVo.getTotalLight()));

                BigDecimal multiBigdecal = powerLightVo.getTotalEconomyLight().multiply(tunnelEdgeComputingTerminal.getCarbonEmissionFactor());
                BigDecimal divide = multiBigdecal.divide(BigDecimal.valueOf(1000), 2, RoundingMode.DOWN);
                powerLightVo.setTheoreticalCarbonEmissionReduction(divide);

            });
            return powerLightVos;
        }
        return Lists.newArrayList();
    }

    @Override
    public DashboardPowerOverviewVo userPowerOverview(DashboardPowerDto dto) {
        Long userId = SecurityUtils.getUserId();
        String year = dto.getYear();
        boolean isAdmin = SecurityUtils.isAdmin(userId);

        // 使用 tunnel_carbon_day_push（按日汇总，仅指定年份 1-12 月有数据）
        // admin 不按 t_user_tunnel 过滤，获取全量数据；其他用户按分配隧道过滤
        List<DashboardMonthlyRawVo> monthlyRaw = isAdmin
                ? getUserPowerFromTunnelCarbonDayPushAll(year)
                : getUserPowerFromTunnelCarbonDayPush(userId, year);

        DashboardPowerOverviewVo vo = new DashboardPowerOverviewVo();
        vo.setAnnualOverview(new DashboardPowerOverviewVo.AnnualOverview());
        vo.getAnnualOverview().setTotalConsumption(BigDecimal.ZERO);
        vo.getAnnualOverview().setTotalSaving(BigDecimal.ZERO);
        vo.getAnnualOverview().setPowerSavingRate(BigDecimal.ZERO);
        vo.getAnnualOverview().setCarbonReductionRate(BigDecimal.ZERO);

        // 获取隧道的设计功率与碳排放因子，用于计算节电量与碳减排率
        // admin 使用全部 level-4 隧道；其他用户使用 t_user_tunnel 分配的隧道
        List<Long> tunnelIds;
        if (isAdmin) {
            tunnelIds = tunnelNameResultService.list(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getLevel, 4))
                    .stream().map(TunnelNameResult::getId).collect(Collectors.toList());
        } else {
            tunnelIds = userTunnelService.list(new LambdaQueryWrapper<UserTunnel>().eq(UserTunnel::getUserId, userId))
                    .stream().map(UserTunnel::getTunnelNameId).distinct().collect(Collectors.toList());
        }
        List<TunnelEdgeComputingTerminal> terminals = CollectionUtils.isEmpty(tunnelIds) ? Collections.emptyList()
                : tunnelEdgeComputingTerminalService.listByIds(tunnelIds);

        int yearInt = Integer.parseInt(year);
        int daysInYear = (yearInt % 4 == 0 && (yearInt % 100 != 0 || yearInt % 400 == 0)) ? 366 : 365;
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();
        // lastMonthIncluded：用于过滤当月及之后月份（未到月份不参与汇总）
        int lastMonthIncluded;
        if (yearInt == currentYear) {
            lastMonthIncluded = currentMonth;
        } else if (yearInt < currentYear) {
            lastMonthIncluded = 12;
        } else {
            lastMonthIncluded = 0;
        }

        // 按“有数据的天数”口径：从 tunnel_carbon_day_push（type=2 电表）获取各隧道当年有数据的日期范围
        // 仅统计有数据的隧道；无数据隧道不参与原设计和实际耗电汇总，避免按自然日带来的误差
        BigDecimal totalOriginalPowerKwh = BigDecimal.ZERO;
        BigDecimal totalOriginalCarbon = BigDecimal.ZERO;
        BigDecimal totalDesignPower = BigDecimal.ZERO;
        Map<Long, Integer> tunnelDaysMap = new HashMap<>();
        Map<String, Integer> tunnelMonthDaysMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(tunnelIds)) {
            List<TunnelDataDaysVo> tunnelDataDays = tunnelCarbonDayPushMapper.selectTunnelDataDaysRange(tunnelIds, year);
            for (TunnelDataDaysVo td : tunnelDataDays) {
                if (td.getFirstDate() != null && td.getLastDate() != null) {
                    int days = (int) ChronoUnit.DAYS.between(td.getFirstDate(), td.getLastDate()) + 1;
                    if (days > 0) {
                        tunnelDaysMap.put(td.getTunnelId(), days);
                    }
                }
            }
            List<TunnelDataDaysByMonthVo> tunnelMonthDays = tunnelCarbonDayPushMapper.selectTunnelDataDaysByMonth(tunnelIds, year);
            for (TunnelDataDaysByMonthVo tm : tunnelMonthDays) {
                if (tm.getTunnelId() != null && tm.getMonth() != null && tm.getDaysInMonth() != null && tm.getDaysInMonth() > 0) {
                    tunnelMonthDaysMap.put(tm.getTunnelId() + "_" + tm.getMonth(), tm.getDaysInMonth());
                }
            }
        }

        Map<Long, TunnelEdgeComputingTerminal> terminalMap = terminals.stream().collect(Collectors.toMap(TunnelEdgeComputingTerminal::getId, t -> t, (a, b) -> a));
        for (Map.Entry<Long, Integer> e : tunnelDaysMap.entrySet()) {
            Long tunnelId = e.getKey();
            int days = e.getValue();
            TunnelEdgeComputingTerminal t = terminalMap.get(tunnelId);
            if (t == null || t.getDesignOperatingPowerTotal() == null || t.getDesignOperatingPowerTotal() <= 0) continue;
            int designPower = t.getDesignOperatingPowerTotal();
            BigDecimal factor = t.getCarbonEmissionFactor() != null ? t.getCarbonEmissionFactor() : BigDecimal.ZERO;
            totalOriginalPowerKwh = totalOriginalPowerKwh.add(BigDecimal.valueOf(designPower * (long) days * 24L));
            totalDesignPower = totalDesignPower.add(BigDecimal.valueOf(designPower));
            if (factor.compareTo(BigDecimal.ZERO) > 0) {
                totalOriginalCarbon = totalOriginalCarbon.add(
                        BigDecimal.valueOf(designPower * (long) days * 24L).multiply(factor).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
            }
        }

        if (CollectionUtils.isEmpty(monthlyRaw)) {
            List<DashboardPowerOverviewVo.MonthlyItem> emptyMonthly = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                DashboardPowerOverviewVo.MonthlyItem item = new DashboardPowerOverviewVo.MonthlyItem();
                item.setMonth(m);
                item.setConsumption(BigDecimal.ZERO);
                item.setSaving(BigDecimal.ZERO);
                emptyMonthly.add(item);
            }
            vo.setMonthlyData(emptyMonthly);
            return vo;
        }

        BigDecimal totalLight = BigDecimal.ZERO;
        Map<Integer, DashboardMonthlyRawVo> monthMap = new HashMap<>();
        for (DashboardMonthlyRawVo r : monthlyRaw) {
            monthMap.put(r.getMonth(), r);
            if (r.getMonth() != null && r.getMonth() <= lastMonthIncluded) {
                totalLight = totalLight.add(r.getTotalLight() != null ? r.getTotalLight() : BigDecimal.ZERO);
            }
        }

        // 实际耗电 kWh（totalLight 为 Wh 时 /1000；接口统一返回 kWh，前端按需换算万kWh）
        BigDecimal totalLightKwh = totalLight.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        vo.getAnnualOverview().setTotalConsumption(totalLightKwh);

        // 节电量 = 原设计(kWh) - 实际(kWh)，接口返回 kWh
        BigDecimal totalSavingKwh = totalOriginalPowerKwh.subtract(totalLightKwh).max(BigDecimal.ZERO);
        vo.getAnnualOverview().setTotalSaving(totalSavingKwh);

        // 节电率 = 节电量 / 原设计 * 100
        if (totalOriginalPowerKwh.compareTo(BigDecimal.ZERO) > 0) {
            vo.getAnnualOverview().setPowerSavingRate(
                    totalSavingKwh.divide(totalOriginalPowerKwh, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        }

        // 碳减排率：有效碳排放因子；实际碳排放 = 实际耗电量(kWh)*有效因子/1000
        if (totalOriginalCarbon.compareTo(BigDecimal.ZERO) > 0 && totalDesignPower.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal effectiveFactor = totalOriginalCarbon.multiply(BigDecimal.valueOf(1000))
                    .divide(totalOriginalPowerKwh, 4, RoundingMode.HALF_UP);
            BigDecimal totalActualCarbon = totalLightKwh.multiply(effectiveFactor).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            BigDecimal carbonReduction = totalOriginalCarbon.subtract(totalActualCarbon);
            vo.getAnnualOverview().setCarbonReductionRate(
                    carbonReduction.divide(totalOriginalCarbon, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        }

        // 各月节电量：按各隧道该月有数据的天数计算原设计，再减实际
        List<DashboardPowerOverviewVo.MonthlyItem> monthlyData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            DashboardPowerOverviewVo.MonthlyItem item = new DashboardPowerOverviewVo.MonthlyItem();
            item.setMonth(m);
            BigDecimal monthConsumption = BigDecimal.ZERO;
            if (monthMap.get(m) != null && monthMap.get(m).getTotalLight() != null) {
                monthConsumption = monthMap.get(m).getTotalLight();
            }
            // 接口统一返回 kWh（totalLight 为 Wh 时 /1000）
            BigDecimal monthConsumptionKwh = monthConsumption.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            item.setConsumption(monthConsumptionKwh);
            if (m > lastMonthIncluded) {
                item.setSaving(BigDecimal.ZERO);
            } else {
                BigDecimal monthOriginalKwh = BigDecimal.ZERO;
                for (Map.Entry<Long, Integer> e : tunnelDaysMap.entrySet()) {
                    Long tunnelId = e.getKey();
                    TunnelEdgeComputingTerminal t = terminalMap.get(tunnelId);
                    if (t == null || t.getDesignOperatingPowerTotal() == null || t.getDesignOperatingPowerTotal() <= 0) continue;
                    Integer daysInMonth = tunnelMonthDaysMap.get(tunnelId + "_" + m);
                    if (daysInMonth != null && daysInMonth > 0) {
                        monthOriginalKwh = monthOriginalKwh.add(BigDecimal.valueOf(t.getDesignOperatingPowerTotal() * (long) daysInMonth * 24L));
                    }
                }
                BigDecimal monthSavingKwh = monthOriginalKwh.subtract(monthConsumptionKwh).max(BigDecimal.ZERO);
                item.setSaving(monthSavingKwh);
            }
            monthlyData.add(item);
        }

        // 暂时去掉：本月理论亮灯时长削减率计算
        for (DashboardPowerOverviewVo.MonthlyItem item : monthlyData) {
            item.setLightUpReductionRate(BigDecimal.ZERO);
        }

        vo.setMonthlyData(monthlyData);
        return vo;
    }

    @Override
    public DashboardTunnelOverviewVo tunnelOverview() {
        Long userId = SecurityUtils.getUserId();
        List<TunnelNameResult> flatList = tunnelNameResultService.getTunnelNameFlatList(userId);
        DashboardTunnelOverviewVo vo = new DashboardTunnelOverviewVo();
        vo.setHighwayCount(0);
        vo.setTunnelCount(0);
        vo.setTotalMileage(BigDecimal.ZERO);
        if (CollectionUtils.isEmpty(flatList)) {
            return vo;
        }
        int highwayCount = 0;
        int tunnelCount = 0;
        BigDecimal totalMileage = BigDecimal.ZERO;
        for (TunnelNameResult n : flatList) {
            Integer level = n.getLevel();
            if (level == null) continue;
            if (level == 2) {
                highwayCount++;
            } else if (level == 3) {
                tunnelCount++;
            } else if (level == 4 && n.getTunnelMileage() != null) {
                // 总里程仅以 level 4（左右线）里程累加，不再使用 level 3 汇总值
                totalMileage = totalMileage.add(n.getTunnelMileage());
            }
        }
        vo.setHighwayCount(highwayCount);
        vo.setTunnelCount(tunnelCount);
        vo.setTotalMileage(totalMileage.setScale(1, RoundingMode.HALF_UP));
        return vo;
    }

    @Override
    public DashboardDeviceStatusVo deviceStatusDistribution() {
        Long userId = SecurityUtils.getUserId();
        // 仅直接分配 + level-3 扩展，不扩展 level-2（避免同名隧道导致多统计）
        List<Long> tunnelIds = tunnelNameResultService.getLevel4TunnelIdsForDeviceStatus(userId);
        if (CollectionUtils.isEmpty(tunnelIds)) {
            tunnelIds = tunnelNameResultService.getLevel4TunnelIdsForUser(userId);
        }
        com.scsdky.web.domain.vo.DeviceTypeVo raw;
        if (CollectionUtils.isEmpty(tunnelIds)) {
            raw = new com.scsdky.web.domain.vo.DeviceTypeVo();
            raw.setDeviceCount(0L);
            raw.setDeviceOnline(0L);
        } else {
            raw = baseMapper.countDeviceStatusMultiTable(tunnelIds);
            if (raw == null) {
                raw = new com.scsdky.web.domain.vo.DeviceTypeVo();
                raw.setDeviceCount(0L);
                raw.setDeviceOnline(0L);
            }
        }
        long total = raw != null && raw.getDeviceCount() != null ? raw.getDeviceCount() : 0L;
        long online = raw != null && raw.getDeviceOnline() != null ? raw.getDeviceOnline() : 0L;
        long offline = total - online;
        DashboardDeviceStatusVo vo = new DashboardDeviceStatusVo();
        vo.setTotal(total);
        vo.setOnline(online);
        vo.setOffline(offline);
        if (total > 0) {
            vo.setOnlinePercent(BigDecimal.valueOf(online).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            vo.setOfflinePercent(BigDecimal.valueOf(offline).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        } else {
            vo.setOnlinePercent(BigDecimal.ZERO);
            vo.setOfflinePercent(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    public DashboardTodayPowerVo todayPowerSummary() {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);
        LocalDate today = LocalDate.now();

        // 批量查询今日实际耗电量（kWh），与 getEnergyCarbonVo 的 LAG 逻辑一致，power_value 存 kWh
        BigDecimal todayConsumptionKwh = Optional.ofNullable(
                tunnelCarbonDayPushMapper.selectTodayPowerConsumptionBatch(isAdmin ? null : userId, today)
        ).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // 今日原设计耗电量（kWh）= designOperatingPowerTotal(kW) * 24，与 statistics 一致
        BigDecimal todayOriginalKwh = Optional.ofNullable(isAdmin
                ? tunnelCarbonDayPushMapper.selectTodayDesignPowerSumAll(today)
                : tunnelCarbonDayPushMapper.selectTodayDesignPowerSumByUser(userId, today)).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal todaySavingKwh = todayOriginalKwh.subtract(todayConsumptionKwh).max(BigDecimal.ZERO);
        BigDecimal carbonFactor = new BigDecimal("0.573");
        BigDecimal todayCarbonReductionKg = todaySavingKwh.multiply(carbonFactor).setScale(2, RoundingMode.HALF_UP);

        DashboardTodayPowerVo vo = new DashboardTodayPowerVo();
        vo.setTodayConsumptionKwh(todayConsumptionKwh);
        vo.setTodaySavingKwh(todaySavingKwh);
        vo.setTodayCarbonReductionKg(todayCarbonReductionKg);
        return vo;
    }

    @Override
    public DashboardLightUpReductionVo lightUpReductionRateCurrentMonth() {
        DashboardLightUpReductionVo vo = new DashboardLightUpReductionVo();
        vo.setLightUpReductionRate(BigDecimal.ZERO);

        Long userId = SecurityUtils.getUserId();
        List<Long> tunnelIds = tunnelNameResultService.getLevel4TunnelIdsForUser(userId);
        if (CollectionUtils.isEmpty(tunnelIds)) {
            return vo;
        }

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        // tunnel_inside_outside_day.upload_time 为日期字符串 yyyy-MM-dd
        String startDate = String.format("%d-%02d-01", year, month);
        String endDate = String.format("%d-%02d-%02d", year, month, day);

        // 总原设计亮灯时长 = Σ(隧道i 本月有数据天数 × 24) 小时（与统计分析一致）
        List<TunnelLightingDaysVo> daysList = tunnelInsideOutsideDayMapper.selectCurrentMonthDataDaysByTunnel(tunnelIds, startDate, endDate);
        BigDecimal totalOriginalHours = BigDecimal.ZERO;
        for (TunnelLightingDaysVo d : daysList) {
            if (d.getDaysInMonth() != null && d.getDaysInMonth() > 0) {
                totalOriginalHours = totalOriginalHours.add(BigDecimal.valueOf(d.getDaysInMonth() * 24L));
            }
        }

        if (totalOriginalHours.compareTo(BigDecimal.ZERO) <= 0) {
            return vo;
        }

        // 总实际亮灯时长：tunnel_inside_outside_day.light_up 单位已是小时（与统计分析-总体分析一致）
        BigDecimal totalActualHours = tunnelInsideOutsideDayMapper.selectCurrentMonthLightUpSum(tunnelIds, startDate, endDate);
        if (totalActualHours == null) {
            totalActualHours = BigDecimal.ZERO;
        }
        totalActualHours = totalActualHours.setScale(2, RoundingMode.HALF_UP);

        // 削减率 = (总原设计 - 总实际) / 总原设计 × 100
        BigDecimal reduction = totalOriginalHours.subtract(totalActualHours).max(BigDecimal.ZERO);
        vo.setLightUpReductionRate(
                reduction.divide(totalOriginalHours, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    /**
     * 从 tunnel_carbon_day_push 获取 admin 全量隧道的月度用电数据（不按用户过滤）。
     */
    private List<DashboardMonthlyRawVo> getUserPowerFromTunnelCarbonDayPushAll(String year) {
        List<PowerLightVo> powerByMonth = tunnelCarbonDayPushMapper.selectAllPowerByMonth(year);
        if (CollectionUtils.isEmpty(powerByMonth)) {
            return Collections.emptyList();
        }
        List<DashboardMonthlyRawVo> result = new ArrayList<>();
        for (PowerLightVo p : powerByMonth) {
            DashboardMonthlyRawVo raw = new DashboardMonthlyRawVo();
            raw.setMonth(p.getMonth() != null ? p.getMonth() : 0);
            raw.setTotalLight(p.getTotalLight() != null ? p.getTotalLight() : BigDecimal.ZERO);
            raw.setTotalEconomyLight(BigDecimal.ZERO);
            raw.setOrigCarbon(BigDecimal.ZERO);
            raw.setActualCarbon(BigDecimal.ZERO);
            result.add(raw);
        }
        return result;
    }

    /**
     * 从 tunnel_carbon_day_push 获取用户已分配隧道的月度用电数据。
     * 一次 SQL 完成，仅返回指定年份 1-12 月，节电量为 0。
     */
    private List<DashboardMonthlyRawVo> getUserPowerFromTunnelCarbonDayPush(Long userId, String year) {
        List<PowerLightVo> powerByMonth = tunnelCarbonDayPushMapper.selectUserPowerByMonth(userId, year);
        if (CollectionUtils.isEmpty(powerByMonth)) {
            return Collections.emptyList();
        }
        List<DashboardMonthlyRawVo> result = new ArrayList<>();
        for (PowerLightVo p : powerByMonth) {
            DashboardMonthlyRawVo raw = new DashboardMonthlyRawVo();
            raw.setMonth(p.getMonth() != null ? p.getMonth() : 0);
            raw.setTotalLight(p.getTotalLight() != null ? p.getTotalLight() : BigDecimal.ZERO);
            raw.setTotalEconomyLight(BigDecimal.ZERO);
            raw.setOrigCarbon(BigDecimal.ZERO);
            raw.setActualCarbon(BigDecimal.ZERO);
            result.add(raw);
        }
        return result;
    }

    @Override
    public Boolean model(ModelDto modelDto) throws JsonProcessingException {
        //设置模式
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = new TunnelEdgeComputingTerminal();
        BeanUtils.copyProperties(modelDto,tunnelEdgeComputingTerminal);
        tunnelEdgeComputingTerminal.setId(modelDto.getTunnelId());
        tunnelEdgeComputingTerminalService.updateById(tunnelEdgeComputingTerminal);
        //记录下发指令
        tunnelSyscmdService.setCmdData(modelDto.getTunnelId(),"DownloadTunnelBaseConfig","","",1);
        return Boolean.TRUE;
    }

    @Override
    public TunnelEdgeComputingTerminal getCurrentModel(Long tunnelId) {
        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId)
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelId);
        tunnelEdgeComputingTerminal.setMode(tunnelDevicelistService.getById(tunnelDevicelistTunnelinfo.getDevicelistId()).getWorkmode());
        return  tunnelEdgeComputingTerminal;
    }

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        System.out.println(decimalFormat.format(new BigDecimal("12.24556")));
    }

    @Override
    public List<EnergyCarbonVo> carbon(AnalyzeDto analyzeDto) throws ParseException {

        List<EnergyCarbonVo> result = new ArrayList<>();
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());

        List<Long> ascUniqueIds = getAllUniqueIdOrAddrId(analyzeDto,tunnelEdgeComputingTerminal,1);
        //排除当天的
        if(analyzeDto.getEndTime().equals(DateUtils.getDate())) {
            String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
            analyzeDto.setEndTime(endTime);
        }

        if(CollectionUtils.isNotEmpty(ascUniqueIds)) {
            List<EnergyCarbonVo> energyCarbonVos = tunnelPowerDataService.selectCountValue(ascUniqueIds, analyzeDto);

            //查询当前电表存在的段
            List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.list(new LambdaQueryWrapper<TunnelPowerEdgeComputing>()
                    .in(TunnelPowerEdgeComputing::getUniqueId, ascUniqueIds)
                    .orderByAsc(TunnelPowerEdgeComputing::getMeterIndex));
            List<String> loopNames = tunnelPowerEdgeComputings.stream().map(TunnelPowerEdgeComputing::getLoopName).collect(Collectors.toList());

            //DecimalFormat decimalFormat = new DecimalFormat("0.0");
            energyCarbonVos.forEach(energyCarbonVo -> {

                List<MeterReadingVo> meterReadings = new ArrayList<>();
                //电表读数
                String powerDataValue = energyCarbonVo.getPowerDataValue();
                String[] powerDataValues = powerDataValue.split(StringPool.COMMA);

                //电表读数 入口段 2
                for (String loopName : loopNames) {
                    boolean exits = true;
                    for (String dataValue : powerDataValues) {
                        String[] loopDataValues = dataValue.split(StringPool.DASH);
                        //如果包含回路直接添加
                        if(loopDataValues[0].equals(loopName)) {
                            MeterReadingVo meterReadingVo = new MeterReadingVo();
                            meterReadingVo.setLoopName(loopName);
                            meterReadingVo.setDataValue(loopDataValues[1]);
                            meterReadings.add(meterReadingVo);
                            exits = false;
                            break;
                        }
                    }
                    //没有任何匹配记录
                    if (exits) {
                        MeterReadingVo meterReadingVo = new MeterReadingVo();
                        meterReadingVo.setLoopName(loopName);
                        meterReadingVo.setDataValue(null);
                        meterReadings.add(meterReadingVo);
                    }
                }

                energyCarbonVo.setMeterReadingVos(meterReadings);
                //理论节电率
                Integer designOperatingPowerTotal = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
                double v2 = (designOperatingPowerTotal * 24  * 1.0 - energyCarbonVo.getDailyPowerConsumption()) / (designOperatingPowerTotal * 24) ;
                energyCarbonVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.UP));
                //理论节电量
                BigDecimal theoreticalPowerSavings = BigDecimal.valueOf(designOperatingPowerTotal * 24).subtract(BigDecimal.valueOf(energyCarbonVo.getDailyPowerConsumption()));
                energyCarbonVo.setTheoreticalPowerSavings(theoreticalPowerSavings.setScale(1,RoundingMode.UP));
                result.add(energyCarbonVo);
            });
            return result;
        }
        return Lists.newArrayList();
    }


    @Override
    public List<EnergyCarbonVo> carbon2(AnalyzeDto analyzeDto) throws ParseException {

        List<EnergyCarbonVo> result = new ArrayList<>();
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //获取所有电表地址
        List<Long> allUniqueId =  getAllUniqueIdOrAddrId(analyzeDto,tunnelEdgeComputingTerminal,1);

        if(!allUniqueId.isEmpty() ) {
            //排除当天的
            if(analyzeDto.getEndTime().equals(DateUtils.getDate())) {
                String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
                analyzeDto.setEndTime(endTime);
            }
            List<EnergyCarbonVo> energyCarbonVos = tunnelPowerDataService.selectCountValue(allUniqueId, analyzeDto);
            List<String> loopNames = Arrays.asList("总回路","前半洞加强","基本","出口","应急","备用");
            for (EnergyCarbonVo energyCarbonVo : energyCarbonVos) {//04-08、04-09
                //每天的各个电表读数
                List<MeterReadingVo> meterReadings = new ArrayList<>();
                //计算各回路的电表总和
                for (String loopName : loopNames) {
                    MeterReadingVo meterReadingVo = new MeterReadingVo();
                    meterReadingVo.setLoopName(loopName);
                    BigDecimal dataValueDecimal = BigDecimal.ZERO;
                    //电表读数 出口加强24-135.7,出口基本25-587.5,出口应急26-3183.1
                    String powerDataValue = energyCarbonVo.getPowerDataValue();
                    String[] powerDataValues = powerDataValue.split(StringPool.COMMA);
                    //回路总表、右侧入口加强、左侧入口加强、右侧过渡加强、左侧过渡加强
                    for (String loopNameValue : powerDataValues) {
                        String[] loopDataValues = loopNameValue.split(StringPool.DASH);
                        //获取各个总回路对应的子回路
                        List<String> loopNameDetail = getLoopNameDetail(loopName);
                        //如果包含回路直接添加
                        if (loopNameDetail.contains(loopDataValues[0])) {
                            String loopDataValue = loopDataValues[1];
                            dataValueDecimal = dataValueDecimal.add(new BigDecimal(loopDataValue));
                        }
                    }
                    meterReadingVo.setDataValue(dataValueDecimal.toString());
                    meterReadings.add(meterReadingVo);
                }

                energyCarbonVo.setMeterReadingVos(meterReadings);

                //理论节电率
                Integer designOperatingPowerTotal = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
                double v2 = (designOperatingPowerTotal * 24 * 1.0 - energyCarbonVo.getDailyPowerConsumption()) / (designOperatingPowerTotal * 24);
                energyCarbonVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.UP));
                //理论节电量
                BigDecimal theoreticalPowerSavings = BigDecimal.valueOf(designOperatingPowerTotal * 24).subtract(BigDecimal.valueOf(energyCarbonVo.getDailyPowerConsumption()));
                energyCarbonVo.setTheoreticalPowerSavings(theoreticalPowerSavings.setScale(1, RoundingMode.UP));
                result.add(energyCarbonVo);
            }
        }

        return result;
    }

    @Override
    public List<EnergyCarbonVo> carbonV2(AnalyzeDto analyzeDto) {
        return tunnelCarbonDayService.list(new LambdaQueryWrapper<TunnelCarbonDay>()
                .eq(TunnelCarbonDay::getTunnelId, analyzeDto.getTunnelId())
                .ge(com.scsdky.common.utils.StringUtils.isNotBlank(analyzeDto.getStartTime()), TunnelCarbonDay::getUploadTime, analyzeDto.getStartTime())
                .le(com.scsdky.common.utils.StringUtils.isNotBlank(analyzeDto.getEndTime()), TunnelCarbonDay::getUploadTime, analyzeDto.getEndTime())
                .orderByDesc(TunnelCarbonDay::getUploadTime)).stream().map(td -> {
            EnergyCarbonVo energyCarbonVo = new EnergyCarbonVo();
            BeanUtils.copyProperties(td, energyCarbonVo);
            energyCarbonVo.setMeterReadingVos(JSONObject.parseArray(td.getMeterReadingVos(),MeterReadingVo.class));
            return energyCarbonVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EnergyCarbonVo> carbonV3(AnalyzeDto analyzeDto) throws ParseException {
        //排除当天的
        if(analyzeDto.getEndTime().equals(DateUtils.getDate())) {
            String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
            analyzeDto.setEndTime(endTime);
        }
        //由于唐总那边不能直接提供电表的唯一id，他存的是地址号和电能表id，所以需要根据电能表的id和电表的地址来查询
        //查询当前隧道下有几个电能终端
        List<TunnelDevicelistTunnelinfo> tunnelDevicelistTunnelinfos = tunnelDevicelistTunnelinfoService
                .list(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                        .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                        .eq(TunnelDevicelistTunnelinfo::getType,2));
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        List<Long> addrIds = getAllUniqueIdOrAddrId(analyzeDto, tunnelEdgeComputingTerminal, 2);

        if(CollectionUtils.isNotEmpty(addrIds)) {
            List<Long> devicelistId = tunnelDevicelistTunnelinfos.stream().map(TunnelDevicelistTunnelinfo::getDevicelistId).collect(Collectors.toList());
            List<EnergyCarbonVo> energyCarbonVos = tunnelCarbonDayPushService.getEnergyCarbonVo(devicelistId,addrIds,analyzeDto);
            List<String> loopNames = Arrays.asList("总回路","前半洞加强","基本","出口","应急","备用");
            for (EnergyCarbonVo energyCarbonVo : energyCarbonVos) {//04-08、04-09
                //每天的各个电表读数
                List<MeterReadingVo> meterReadings = new ArrayList<>();
                //计算各回路的电表总和
                for (String loopName : loopNames) {
                    MeterReadingVo meterReadingVo = new MeterReadingVo();
                    meterReadingVo.setLoopName(loopName);
                    BigDecimal dataValueDecimal = BigDecimal.ZERO;
                    //电表读数 出口加强24-135.7,出口基本25-587.5,出口应急26-3183.1
                    String powerDataValue = energyCarbonVo.getPowerDataValue();
                    String[] powerDataValues = powerDataValue.split(StringPool.COMMA);
                    //回路总表、右侧入口加强、左侧入口加强、右侧过渡加强、左侧过渡加强
                    for (String loopNameValue : powerDataValues) {
                        String[] loopDataValues = loopNameValue.split(StringPool.DASH);
                        //获取各个总回路对应的子回路
                        List<String> loopNameDetail = getLoopNameDetail(loopName);
                        //如果包含回路直接添加
                        if (loopNameDetail.contains(loopDataValues[0])) {
                            String loopDataValue = loopDataValues[1];
                            dataValueDecimal = dataValueDecimal.add(new BigDecimal(loopDataValue));
                        }
                    }
                    meterReadingVo.setDataValue(dataValueDecimal.toString());
                    meterReadings.add(meterReadingVo);
                }

                energyCarbonVo.setMeterReadingVos(meterReadings);

                //理论节电率
                Integer designOperatingPowerTotal = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
                double v2 = (designOperatingPowerTotal * 24 * 1.0 - (energyCarbonVo.getDailyPowerConsumption() == null ? 0:energyCarbonVo.getDailyPowerConsumption())) / (designOperatingPowerTotal * 24);
                energyCarbonVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.UP));
                //理论节电量
                BigDecimal theoreticalPowerSavings = BigDecimal.valueOf(designOperatingPowerTotal * 24).subtract(BigDecimal.valueOf(energyCarbonVo.getDailyPowerConsumption() == null ? 0:energyCarbonVo.getDailyPowerConsumption()));
                energyCarbonVo.setTheoreticalPowerSavings(theoreticalPowerSavings.setScale(1, RoundingMode.UP));
            }
            return energyCarbonVos;
        }
        return Lists.newArrayList();
    }

    @Override
    public List<EnergyCarbonVo> carbonV4(AnalyzeDto analyzeDto) throws ParseException {
        //排除当天的
        if(analyzeDto.getEndTime().equals(DateUtils.getDate())) {
            String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
            analyzeDto.setEndTime(endTime);
        }
        //由于唐总那边不能直接提供电表的唯一id，他存的是地址号和电能表id，所以需要根据电能表的id和电表的地址来查询
        //查询当前隧道下有几个电能终端
        List<TunnelDevicelistTunnelinfo> tunnelDevicelistTunnelinfos = tunnelDevicelistTunnelinfoService
                .list(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                        .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                        .eq(TunnelDevicelistTunnelinfo::getType,2));
        //通过隧道信息，查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        List<Long> addrIds = getAllUniqueIdOrAddrId(analyzeDto, tunnelEdgeComputingTerminal, 2);
        if(CollectionUtils.isNotEmpty(addrIds)) {
            List<Long> devicelistId = tunnelDevicelistTunnelinfos.stream().map(TunnelDevicelistTunnelinfo::getDevicelistId).collect(Collectors.toList());
            //获取能碳数据统计
            List<EnergyCarbonVo> energyCarbonVos = getEnergyCarbonVos(analyzeDto, addrIds, devicelistId);
            List<String> loopNames = Arrays.asList("总回路","前半洞加强","基本","出口","应急","备用");
            for (EnergyCarbonVo energyCarbonVo : energyCarbonVos) {//04-08、04-09
                //每天的各个电表读数
                List<MeterReadingVo> meterReadings = new ArrayList<>();
                //计算各回路的电表总和
                for (String loopName : loopNames) {
                    MeterReadingVo meterReadingVo = new MeterReadingVo();
                    meterReadingVo.setLoopName(loopName);
                    BigDecimal dataValueDecimal = BigDecimal.ZERO;
                    //电表读数 出口加强24-135.7,出口基本25-587.5,出口应急26-3183.1
                    String powerDataValue = energyCarbonVo.getPowerDataValue();
                    String[] powerDataValues = powerDataValue.split(StringPool.COMMA);
                    //回路总表、右侧入口加强、左侧入口加强、右侧过渡加强、左侧过渡加强
                    for (String loopNameValue : powerDataValues) {
                        String[] loopDataValues = loopNameValue.split(StringPool.DASH);
                        //获取各个总回路对应的子回路
                        List<String> loopNameDetail = getLoopNameDetail(loopName);
                        //如果包含回路直接添加
                        if (loopNameDetail.contains(loopDataValues[0])) {
                            String loopDataValue = loopDataValues[1];
                            dataValueDecimal = dataValueDecimal.add(new BigDecimal(loopDataValue));
                        }
                    }
                    meterReadingVo.setDataValue(dataValueDecimal.toString());
                    meterReadings.add(meterReadingVo);
                }

                energyCarbonVo.setMeterReadingVos(meterReadings);

                //理论节电率
                Integer designOperatingPowerTotal = tunnelEdgeComputingTerminal.getDesignOperatingPowerTotal();
                double v2 = (designOperatingPowerTotal * 24 * 1.0 - (energyCarbonVo.getDailyPowerConsumption() == null ? 0:energyCarbonVo.getDailyPowerConsumption())) / (designOperatingPowerTotal * 24);
                energyCarbonVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.UP));
                //理论节电量
                BigDecimal theoreticalPowerSavings = BigDecimal.valueOf(designOperatingPowerTotal * 24).subtract(BigDecimal.valueOf(energyCarbonVo.getDailyPowerConsumption() == null ? 0:energyCarbonVo.getDailyPowerConsumption()));
                energyCarbonVo.setTheoreticalPowerSavings(theoreticalPowerSavings.setScale(1, RoundingMode.UP));
            }
            return energyCarbonVos;
        }
        return Lists.newArrayList();
    }

    /**
     * 获取能碳数据统计
     * @param analyzeDto 请求参数
     * @param addrIds 地址数组
     * @param devicelistId 电能终端数组
     * @return
     */
    private List<EnergyCarbonVo> getEnergyCarbonVos(AnalyzeDto analyzeDto, List<Long> addrIds, List<Long> devicelistId) {
        List<AddrEnergyCarbonVo> addrEnergyCarbonVos = tunnelCarbonDayPushService.getEnergyCarbonVo4(devicelistId, addrIds, analyzeDto);
        //时间分组
        Map<String, List<AddrEnergyCarbonVo>> map = addrEnergyCarbonVos.stream().collect(Collectors.groupingBy(AddrEnergyCarbonVo::getUploadTime));
        ArrayList<EnergyCarbonVo> carbonVos = new ArrayList<>();
        for (Map.Entry<String, List<AddrEnergyCarbonVo>> entry : map.entrySet()) {
            List<AddrEnergyCarbonVo> value = entry.getValue();
            //查询所有addr最新的数据
            AnalyzeDto dto = new AnalyzeDto();
            dto.setEndTime(entry.getKey());
            //查询小于等于当前时间的最新数据
            List<AddrEnergyCarbonVo> latestDataByAddrIds = tunnelCarbonDayPushService.getLatestDataByAddrIds(devicelistId, addrIds,dto);
            //添加电表地址缺少的数据到value数组中
            addLackData(latestDataByAddrIds, value);
            //组装返回对象
            setEnergyCarbonVoValue(carbonVos, entry, value);
        }
        //组装返回对象
        List<EnergyCarbonVo> energyCarbonVos = getEnergyCarbonVos(carbonVos);
        return energyCarbonVos;
    }

    /**
     * 组装返回对象
     * @param carbonVos carbonVos返回数组
     * @return
     */
    private static List<EnergyCarbonVo> getEnergyCarbonVos(ArrayList<EnergyCarbonVo> carbonVos) {
        //排序
        List<EnergyCarbonVo> energyCarbonVos = carbonVos.stream().sorted(Comparator.comparing(EnergyCarbonVo::getUploadTime).reversed()).collect(Collectors.toList());
        //计算当天值
        for (EnergyCarbonVo vo : energyCarbonVos) {
            LocalDate uploadTime = vo.getUploadTime();
            List<EnergyCarbonVo> list = energyCarbonVos.stream().filter(item -> item.getUploadTime().equals(uploadTime.minusDays(1))).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(list)){
                continue;
            }
            Double cumulativePowerConsumption = list.get(0).getCumulativePowerConsumption();
            BigDecimal dailyPowerConsumption = new BigDecimal(vo.getCumulativePowerConsumption()).subtract(new BigDecimal(cumulativePowerConsumption));
            vo.setDailyPowerConsumption(dailyPowerConsumption.setScale(2,RoundingMode.UP).doubleValue());
        }
        //去掉最后一条多于的数据
        if (!ObjectUtils.isEmpty(energyCarbonVos)){
            energyCarbonVos.remove(energyCarbonVos.size() -1);
        }
        return energyCarbonVos;
    }

    /**
     * 组装返回对象
     * @param carbonVos 返回数组
     * @param entry 分组对象
     * @param value 统计对象
     */
    private static void setEnergyCarbonVoValue(ArrayList<EnergyCarbonVo> carbonVos, Map.Entry<String, List<AddrEnergyCarbonVo>> entry, List<AddrEnergyCarbonVo> value) {
        BigDecimal count = value.stream().map(AddrEnergyCarbonVo::getPowerValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        EnergyCarbonVo energyCarbonVo = new EnergyCarbonVo();
        energyCarbonVo.setUploadTime(LocalDate.parse(entry.getKey()));
        energyCarbonVo.setCumulativePowerConsumption(count.doubleValue());
        energyCarbonVo.setPowerDataValue(getPowerDataValue(value));
        carbonVos.add(energyCarbonVo);
    }

    /**
     * 获取功率值拼接字符串
     * @param value 统计的数组
     * @return 功率值拼接字符串
     */
    private static String getPowerDataValue(List<AddrEnergyCarbonVo> value) {
        //获得powerDataValue
        StringBuilder powerDataValue = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            AddrEnergyCarbonVo vo = value.get(i);
            powerDataValue.append(vo.getLoopName()).append("-").append(vo.getPowerValue());
            if (i == value.size() - 1){
                continue;
            }
            powerDataValue.append(",");
        }
        return powerDataValue.toString();
    }

    /**
     * 添加电表地址缺少的数据到value数组中
     * @param latestDataByAddrIds 所有addr最新的数据
     * @param value 需要统计的数组
     */
    private void addLackData(List<AddrEnergyCarbonVo> latestDataByAddrIds, List<AddrEnergyCarbonVo> value) {
        List<String> addrList = value
                .stream()
                .map(item -> item.getAddr() + "_" + item.getDevicelistId())
                .collect(Collectors.toList());
        //如果该addr没有数据，就用最新的，并且统计重新计算
        List<AddrEnergyCarbonVo> pushList = latestDataByAddrIds
                .stream()
                .filter(item -> !addrList.contains(item.getAddr() + "_" + item.getDevicelistId())).collect(Collectors.toList());
        value.addAll(pushList);
    }


    /**
     * 获取电能终端下面的所有电表地址
     * @param tunnelDevicelistTunnelinfos 电能终端数组
     * @param direction 1 右线 2 左线
     * @param allUniqueIdOrAddrIds 返回的所有电表地址
     * @param type 1 查询电表唯一id  2 电表地址号
     */
    private List<Long> getAllUniqueIdOrAddrId(List<TunnelDevicelistTunnelinfo> tunnelDevicelistTunnelinfos,int direction,List<Long> allUniqueIdOrAddrIds,int type){
        //通过编号查询左右线对应的电表地址
        for (TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo : tunnelDevicelistTunnelinfos) {
            //电表电能终端号
            Long devicelistId = tunnelDevicelistTunnelinfo.getDevicelistId();
            //查询已配置的电表数据
            LambdaQueryWrapper<TunnelPowerEdgeComputing> tunnelPowerEdgeComputingLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //电表唯一id
            if(type == 1) {
                getAllUniqueId(direction, allUniqueIdOrAddrIds, devicelistId, tunnelPowerEdgeComputingLambdaQueryWrapper);
            }
            //电表唯一地址号
            else if (type == 2) {
                tunnelPowerEdgeComputingLambdaQueryWrapper.select(TunnelPowerEdgeComputing::getAddress);
                tunnelPowerEdgeComputingLambdaQueryWrapper.eq(TunnelPowerEdgeComputing::getDevicelistId, devicelistId);
                tunnelPowerEdgeComputingLambdaQueryWrapper.eq(TunnelPowerEdgeComputing::getDirection, direction);
                tunnelPowerEdgeComputingLambdaQueryWrapper.last("order by meter_index ");
                List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.list(tunnelPowerEdgeComputingLambdaQueryWrapper);
                List<Long> addrs = tunnelPowerEdgeComputings.stream().map(tunnelPowerEdgeComputing -> Long.valueOf(tunnelPowerEdgeComputing.getAddress())).collect(Collectors.toList());
                allUniqueIdOrAddrIds.addAll(addrs);
            }else{
                getAllUniqueId(direction, allUniqueIdOrAddrIds, devicelistId, tunnelPowerEdgeComputingLambdaQueryWrapper);
            }
        }

        return allUniqueIdOrAddrIds;
    }

    private void getAllUniqueId(int direction, List<Long> allUniqueIds, Long devicelistId, LambdaQueryWrapper<TunnelPowerEdgeComputing> tunnelPowerEdgeComputingLambdaQueryWrapper) {
        tunnelPowerEdgeComputingLambdaQueryWrapper.select(TunnelPowerEdgeComputing::getUniqueId);
        tunnelPowerEdgeComputingLambdaQueryWrapper.eq(TunnelPowerEdgeComputing::getDevicelistId, devicelistId);
        tunnelPowerEdgeComputingLambdaQueryWrapper.eq(TunnelPowerEdgeComputing::getDirection, direction);
        tunnelPowerEdgeComputingLambdaQueryWrapper.last("order by meter_index ");
        List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.list(tunnelPowerEdgeComputingLambdaQueryWrapper);
        List<Long> uniqueIds = tunnelPowerEdgeComputings.stream().map(TunnelPowerEdgeComputing::getUniqueId).collect(Collectors.toList());
        allUniqueIds.addAll(uniqueIds);
    }


    private List<String>  getLoopNameDetail(String loopName){
        // 建立映射关系
        Map<String, List<String>> valueMap = new HashMap<>();
        valueMap.put("总回路", Collections.singletonList("回路总表"));
        valueMap.put("前半洞加强", Arrays.asList("右侧入口加强", "左侧入口加强","右侧过渡加强", "左侧过渡加强"));
        //valueMap.put("加强", Arrays.asList("右侧入口加强", "左侧入口加强"));
        //valueMap.put("过渡", Arrays.asList("右侧过渡加强", "左侧过渡加强"));
        valueMap.put("基本", Arrays.asList("基本1", "基本2"));
        valueMap.put("出口", Arrays.asList("出口","出口加强"));
        valueMap.put("应急", Collections.singletonList("应急"));
        valueMap.put("备用", Collections.singletonList("备用"));

        return valueMap.get(loopName);
    }

}




