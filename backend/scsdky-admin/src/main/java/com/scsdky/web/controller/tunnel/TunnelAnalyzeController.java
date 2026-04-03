package com.scsdky.web.controller.tunnel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.PowerLightVo;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DashboardPowerDto;
import com.scsdky.web.domain.dto.HomePageDto;
import com.scsdky.web.domain.dto.ModelDto;
import com.scsdky.web.domain.response.CommonResponse;
import com.scsdky.web.domain.response.CommonUtils;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.TrafficFlowOrSpeedVo;
import com.scsdky.web.domain.vo.monitor.DnVo;
import com.scsdky.web.domain.vo.monitor.LightOutsideVo;
import com.scsdky.web.domain.vo.monitor.SpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficVo;
import com.scsdky.web.domain.vo.dashboard.DashboardDeviceStatusVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMessageCategoryVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPendingCountsVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPowerOverviewVo;
import com.scsdky.web.domain.vo.dashboard.DashboardLightUpReductionVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTodayPowerVo;
import com.scsdky.web.domain.vo.dashboard.DashboardTunnelOverviewVo;
import com.scsdky.web.domain.vo.statistics.CarbonVo;
import com.scsdky.web.domain.vo.statistics.StatisticsVo;
import com.scsdky.web.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;


/**
 * @author tubo
 * @date 2023/09/18
 */
@RestController
@RequestMapping("/analyze")
@Api(value = "分析模块", tags = {"DIRECTOR 1.7：分析模块"})
public class TunnelAnalyzeController extends BaseController {

    @Resource
    private TunnelTrafficFlowService tunnelTrafficFlowService;

    @Resource
    private TunnelLightOutsideService tunnelLightOutsideService;

    @Resource
    private TunnelEnergyCarbonService tunnelEnergyCarbonService;

    @Resource
    private TunnelStatisticsService tunnelStatisticsService;

    @Resource
    private TunnelNoticeService tunnelNoticeService;

    @ApiOperation("统计分析")
    @PostMapping(value = "/statistics",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "统计分析", response = StatisticsVo.class)
    })
    public AjaxResult statistics(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        return AjaxResult.success(tunnelStatisticsService.statistics(analyzeDto));
    }

    @ApiOperation("统计分析--excel导出")
    @PostMapping(value = "/statisticsExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void statisticsExport(HttpServletResponse response,AnalyzeDto analyzeDto) throws ParseException {
        tunnelStatisticsService.exportStatistics(response,analyzeDto);
    }

    @ApiOperation("车流/车速")
    @PostMapping(value = "/trafficFlowOrSpeed",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "车流/车速", response = TrafficFlowOrSpeedVo.class)
    })
    public AjaxResult trafficFlowOrSpeed(@RequestBody @Valid AnalyzeDto analyzeDto) {
        //v1版本
        //return AjaxResult.success(tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto));
        return AjaxResult.success(tunnelTrafficFlowService.trafficFlowOrSpeedV2(analyzeDto));
    }

    @ApiOperation("车流/车速--按小时统计")
    @PostMapping(value = "/trafficFlowOrSpeedByHour",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "车流/车速-按小时统计", response = TrafficFlowOrSpeedVo.class)
    })
    public AjaxResult trafficFlowOrSpeedByHour(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        analyzeDto.setIsHour(1);
        return AjaxResult.success(tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto));
    }

    @ApiOperation("车流/车速--excel导出")
    @PostMapping(value = "/trafficFlowOrSpeedExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void trafficFlowOrSpeedExport(HttpServletResponse response,AnalyzeDto analyzeDto) {
        tunnelTrafficFlowService.exportTraffic(response, analyzeDto);
    }

    @ApiOperation("车流/车速列表--excel导出")
    @PostMapping(value = "/trafficFlowOrSpeedListExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void trafficFlowOrSpeedListExport(HttpServletResponse response,AnalyzeDto analyzeDto) {
        //v1版本
        //List<TrafficFlowOrSpeedVo> speedVos = tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto);
        List<TrafficFlowOrSpeedVo> speedVos = tunnelTrafficFlowService.trafficFlowOrSpeedV2(analyzeDto);
        ExcelUtil<TrafficFlowOrSpeedVo> util = new ExcelUtil<>(TrafficFlowOrSpeedVo.class);
        util.exportExcel(response, speedVos, "车流、车速");
        //tunnelTrafficFlowService.exportTraffic(response, analyzeDto);
    }

    @ApiOperation("洞内外照度")
    @PostMapping(value = "/insideAndOutside",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "洞内外照度", response = InsideAndOutsideVo.class)
    })
    public AjaxResult insideAndOutside(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        //v1暂时关闭
        //return AjaxResult.success(tunnelLightOutsideService.insideAndOutside(analyzeDto));
        return AjaxResult.success(tunnelLightOutsideService.insideAndOutsideV2(analyzeDto));
    }

    @ApiOperation("洞内外照度--按小时")
    @PostMapping(value = "/insideAndOutsideByHour",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "洞内外照度-按小时", response = InsideAndOutsideVo.class)
    })
    public AjaxResult insideAndOutsideByHour(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        analyzeDto.setIsHour(1);
        return AjaxResult.success(tunnelLightOutsideService.insideAndOutside(analyzeDto));
    }

    @ApiOperation("洞内外照度--excel导出")
    @PostMapping(value = "/insideAndOutsideExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void insideAndOutsideExport(HttpServletResponse response,AnalyzeDto analyzeDto) {
        tunnelLightOutsideService.exportLightOutside(response,analyzeDto);
    }

    @ApiOperation("洞内外照度列表--excel导出")
    @PostMapping(value = "/insideAndOutsideListExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void insideAndOutsideListExport(HttpServletResponse response,AnalyzeDto analyzeDto) throws ParseException {
        //List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutside(analyzeDto);
        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutsideV2(analyzeDto);
        ExcelUtil<InsideAndOutsideVo> util = new ExcelUtil<>(InsideAndOutsideVo.class);
        util.exportExcel(response, insideAndOutsideVos, "洞内外列表");
    }

    @ApiOperation("能碳数据")
    @PostMapping(value = "/carbon",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "能碳数据", response = EnergyCarbonVo.class)
    })
    public AjaxResult carbon(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        //v1版本封存
        //return AjaxResult.success(tunnelStatisticsService.carbon(analyzeDto));
        //v2封存
        //return AjaxResult.success(tunnelStatisticsService.carbonV2(analyzeDto));
        //v3版本封存
        //return AjaxResult.success(tunnelStatisticsService.carbonV4(analyzeDto));
        //v4版本
        return AjaxResult.success(tunnelStatisticsService.carbonV4(analyzeDto));
    }


    @ApiOperation("1111")
    @PostMapping(value = "/test",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "1111", response = EnergyCarbonVo.class)
    })
    public AjaxResult carbon2(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        //v1版本封存
        //return AjaxResult.success(tunnelStatisticsService.carbon(analyzeDto));
        return AjaxResult.success(tunnelStatisticsService.carbon2(analyzeDto));
    }

    @ApiOperation("能碳数据--按小时")
    @PostMapping(value = "/carbonByHour",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "能碳数据-按小时", response = EnergyCarbonVo.class)
    })
    public AjaxResult carbonByHour(@RequestBody @Valid AnalyzeDto analyzeDto) {
        analyzeDto.setIsHour(1);
        return AjaxResult.success(tunnelEnergyCarbonService.carbon(analyzeDto));
    }

    @ApiOperation("能碳数据--excel导出")
    @PostMapping(value = "/carbonExport",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void carbonExport(HttpServletResponse response,AnalyzeDto analyzeDto) throws ParseException {
        //List<EnergyCarbonVo> carbon = tunnelStatisticsService.carbon(analyzeDto);
        //List<EnergyCarbonVo> carbon = tunnelStatisticsService.carbonV2(analyzeDto);
//        List<EnergyCarbonVo> carbon = tunnelStatisticsService.carbonV3(analyzeDto);
        List<EnergyCarbonVo> carbon = tunnelStatisticsService.carbonV4(analyzeDto);
        ExcelUtil<EnergyCarbonVo> util = new ExcelUtil<>(EnergyCarbonVo.class);
        util.exportExcel(response, carbon, "能碳数据");
        //tunnelEnergyCarbonService.exportCarbon(response,analyzeDto);
    }


    @ApiOperation("能碳数据--碳排放量")
    @PostMapping(value = "/getCarbonByStatistics",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "统计分析", response = CarbonVo.class)
    })
    public AjaxResult getCarbonByStatistics(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        return AjaxResult.success(tunnelStatisticsService.getCarbonByStatistics(analyzeDto));
    }

    @ApiOperation("照度对比--小时统计")
    @PostMapping(value = "/zdByHouse",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "照度对比--小时统计", response = LightOutsideVo.class)
    })
    public AjaxResult zdByHouse(@RequestBody @Valid AnalyzeDto analyzeDto) {
        return AjaxResult.success(tunnelLightOutsideService.zdByHouse(analyzeDto));
    }

    @ApiOperation("统计车流量--小时统计")
    @PostMapping(value = "/clByHouse",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "统计车流量--小时统计", response = TrafficVo.class)
    })
    public AjaxResult clByHouse(@RequestBody @Valid AnalyzeDto analyzeDto) {
        return AjaxResult.success(tunnelTrafficFlowService.clByHouse(analyzeDto));
    }

    @ApiOperation("实时车速--小时统计")
    @PostMapping(value = "/csByHouse",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "实时车速--小时统计", response = SpeedVo.class)
    })
    public AjaxResult csByHouse(@RequestBody @Valid AnalyzeDto analyzeDto) {
        return AjaxResult.success(tunnelTrafficFlowService.csByHouse(analyzeDto));
    }

    @ApiOperation("电能参数--小时统计")
    @PostMapping(value = "/dnByHouse",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "电能参数--小时统计", response = DnVo.class)
    })
    public AjaxResult dnByHouse(@RequestBody @Valid AnalyzeDto analyzeDto) {
        return AjaxResult.success(tunnelStatisticsService.dnByHouse(analyzeDto));
    }


    @ApiOperation("首页--总用电和节电/月份统计")
    @PostMapping(value = "/lightByMonth",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "首页--总用电和节电/月份统计", response = PowerLightVo.class)
    })
    public AjaxResult lightByMonth(@RequestBody @Valid HomePageDto homePageDto) {
        return AjaxResult.success(tunnelStatisticsService.lightByMonth(homePageDto));
    }

    @ApiOperation("Dashboard--当前用户隧道用电/节电概览（年度+月度）")
    @PostMapping(value = "/dashboard/userPowerOverview", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Dashboard用电节电概览", response = DashboardPowerOverviewVo.class)
    })
    public AjaxResult userPowerOverview(@RequestBody @Valid DashboardPowerDto dto) {
        return AjaxResult.success(tunnelStatisticsService.userPowerOverview(dto));
    }

    @ApiOperation("Dashboard--隧道概况（高速公路数、隧道总数、总里程）")
    @GetMapping(value = "/dashboard/tunnelOverview", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "隧道概况", response = DashboardTunnelOverviewVo.class)
    })
    public AjaxResult tunnelOverview() {
        return AjaxResult.success(tunnelStatisticsService.tunnelOverview());
    }

    @ApiOperation("Dashboard--设备状态分布（统一查询：按用户 level-4 隧道统计，在线/离线，离线=总数-在线）")
    @GetMapping(value = "/dashboard/deviceStatusDistribution", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "设备状态分布", response = DashboardDeviceStatusVo.class)
    })
    public AjaxResult deviceStatusDistribution() {
        return AjaxResult.success(tunnelStatisticsService.deviceStatusDistribution());
    }

    @ApiOperation("Dashboard--今日数据汇总（今日用电量、今日节电量、今日碳减排）")
    @GetMapping(value = "/dashboard/todayPowerSummary", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "今日数据汇总", response = DashboardTodayPowerVo.class)
    })
    public AjaxResult todayPowerSummary() {
        return AjaxResult.success(tunnelStatisticsService.todayPowerSummary());
    }

    @ApiOperation("Dashboard--本月理论亮灯时长削减率（用户管理所有隧道汇总，本月1号00:00~今日23:59，有数据天数口径）")
    @GetMapping(value = "/dashboard/lightUpReductionRate", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "本月理论亮灯时长削减率", response = DashboardLightUpReductionVo.class)
    })
    public AjaxResult lightUpReductionRate() {
        return AjaxResult.success(tunnelStatisticsService.lightUpReductionRateCurrentMonth());
    }

    @ApiOperation("Dashboard--待处理统计（RightSidebar 待处理卡片）")
    @GetMapping(value = "/dashboard/pendingCounts", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "待处理统计", response = DashboardPendingCountsVo.class)
    })
    public AjaxResult pendingCounts() {
        return AjaxResult.success(tunnelNoticeService.pendingCounts());
    }

    @ApiOperation("Dashboard--消息通知列表（RightSidebar 消息通知卡片）")
    @GetMapping(value = "/dashboard/messageNotifications", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "消息通知列表", response = DashboardMessageCategoryVo.class)
    })
    public AjaxResult messageNotifications(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return AjaxResult.success(tunnelNoticeService.messageNotifications(limit));
    }

    @ApiOperation("模式设置")
    @PostMapping(value = "/model",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult model(@RequestBody @Valid ModelDto modelDto) throws JsonProcessingException {
        return AjaxResult.success(tunnelStatisticsService.model(modelDto));
    }

    @ApiOperation("获取当前模式")
    @GetMapping(value = "/get/current/model",produces = {MediaType.APPLICATION_JSON_VALUE})
    public CommonResponse<TunnelEdgeComputingTerminal> getCurrentModel(@RequestParam @Valid Long tunnelId) {
        return CommonUtils.success(tunnelStatisticsService.getCurrentModel(tunnelId));
    }
}
