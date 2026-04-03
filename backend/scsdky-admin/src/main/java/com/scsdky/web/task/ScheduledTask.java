package com.scsdky.web.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.MeterReadingVo;
import com.scsdky.web.domain.vo.TrafficFlowOrSpeedVo;
import com.scsdky.web.service.SchedulerConfigService;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务统一执行类
 */
@Component
public class ScheduledTask {


    @Resource
    private TunnelTrafficFlowDayService tunnelTrafficFlowDayService;

    @Resource
    private TunnelTrafficFlowService tunnelTrafficFlowService;

    @Resource
    private TunnelInsideOutsideDayService tunnelInsideOutsideDayService;

    @Resource
    private TunnelLightOutsideService tunnelLightOutsideService;

    @Resource
    private TunnelCarbonDayService tunnelCarbonDayService;

    @Resource
    private TunnelStatisticsService tunnelStatisticsService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelPowerDataService tunnelPowerDataService;

    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private SchedulerConfigService schedulerConfigService;

    boolean flag = true;
    //同步车流/车速，将每天的数据存起来，方便页面更快的检索
    @Scheduled(cron = "0 30 0 * * ? ")
    //@Scheduled(cron = "0 0/1 * * * ? ")
    public void syncTrafficFlowDay() throws ParseException {
        if (!schedulerConfigService.isSyncTrafficFlowDayEnabled()) {
            return;
        }
        AnalyzeDto analyzeDto = new AnalyzeDto();
        //获取所有的隧道id
        List<Long> tunnelIds = tunnelEdgeComputingTerminalService.list().stream().map(TunnelEdgeComputingTerminal::getId).collect(Collectors.toList());
        for (Long tunnelId : tunnelIds) {
            setTime("","",analyzeDto);
            analyzeDto.setTunnelId(tunnelId);
            List<TrafficFlowOrSpeedVo> trafficFlowOrSpeedVos = tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto);
            List<TunnelTrafficFlowDay> tunnelTrafficFlowDays = new ArrayList<>();
            for (TrafficFlowOrSpeedVo trafficFlowOrSpeedVo : trafficFlowOrSpeedVos) {
                TunnelTrafficFlowDay tunnelTrafficFlowDay = new TunnelTrafficFlowDay();
                BeanUtils.copyProperties(trafficFlowOrSpeedVo, tunnelTrafficFlowDay);
                tunnelTrafficFlowDay.setTunnelId(tunnelId);
                tunnelTrafficFlowDays.add(tunnelTrafficFlowDay);
            }
            tunnelTrafficFlowDayService.saveBatch(tunnelTrafficFlowDays);

        }
    }


    //同步洞内外亮度，将每天的数据存起来，方便页面更快的检索--完成
    @Scheduled(cron = "0 20 0 * * ? ")
    public void syncInsideOutsideDay() throws ParseException {
        if (!schedulerConfigService.isSyncInsideOutsideDayEnabled()) {
            return;
        }
        AnalyzeDto analyzeDto = new AnalyzeDto();
        //获取所有的隧道id
        List<Long> tunnelIds = tunnelEdgeComputingTerminalService.list().stream().map(TunnelEdgeComputingTerminal::getId).collect(Collectors.toList());
        for (Long tunnelId : tunnelIds) {
            setTime("","",analyzeDto);
            analyzeDto.setTunnelId(tunnelId);
            List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutside(analyzeDto);
            List<TunnelInsideOutsideDay> tunnelInsideOutsideDays = new ArrayList<>();

            for (InsideAndOutsideVo insideAndOutsideVo : insideAndOutsideVos) {
                TunnelInsideOutsideDay tunnelInsideOutsideDay = new TunnelInsideOutsideDay();
                BeanUtils.copyProperties(insideAndOutsideVo, tunnelInsideOutsideDay);
                tunnelInsideOutsideDay.setTunnelId(tunnelId);
                tunnelInsideOutsideDays.add(tunnelInsideOutsideDay);
            }
            tunnelInsideOutsideDayService.saveBatch(tunnelInsideOutsideDays);
        }
    }


    //同步能碳数据，将每天的数据存起来，方便页面更快的检索--完成
    @Scheduled(cron = "0 10 0 * * ? ")
    //@Scheduled(cron = "0 0/1 * * * ? ")
    public void syncCarbonDay() throws ParseException {
        if (!schedulerConfigService.isSyncCarbonDayEnabled()) {
            return;
        }
        AnalyzeDto analyzeDto = new AnalyzeDto();
        //获取所有的隧道id
        List<Long> tunnelIds = tunnelEdgeComputingTerminalService.list().stream().map(TunnelEdgeComputingTerminal::getId).collect(Collectors.toList());
        for (Long tunnelId : tunnelIds) {
            setTime("","",analyzeDto);
            analyzeDto.setTunnelId(tunnelId);
            List<EnergyCarbonVo> carbons = tunnelStatisticsService.carbon2(analyzeDto);
            List<TunnelCarbonDay> tunnelCarbonDays = new ArrayList<>();

            for (EnergyCarbonVo energyCarbonVo : carbons) {
                TunnelCarbonDay tunnelCarbonDay = new TunnelCarbonDay();
                List<MeterReadingVo> meterReadingVos = energyCarbonVo.getMeterReadingVos();

                BeanUtils.copyProperties(energyCarbonVo, tunnelCarbonDay);
                tunnelCarbonDay.setMeterReadingVos(JSON.toJSONString(meterReadingVos));
                tunnelCarbonDay.setTunnelId(tunnelId);
                tunnelCarbonDays.add(tunnelCarbonDay);
            }
            tunnelCarbonDayService.saveBatch(tunnelCarbonDays);
        }

    }


    //检查电表数据
    @Scheduled(cron = "0 40 0 * * ? ")
    public void checkPowerData() throws ParseException {
        if (!schedulerConfigService.isCheckPowerDataEnabled()) {
            return;
        }
        AnalyzeDto analyzeDto = new AnalyzeDto();
        //获取所有的电能终端id
        List<Long> energyIds = tunnelDevicelistService.list(Wrappers.lambdaQuery(TunnelDevicelist.class).eq(TunnelDevicelist::getDeviceTypeId,2)).stream().map(TunnelDevicelist::getDeviceId).collect(Collectors.toList());
        for (Long energyId : energyIds) {
            setTime("","",analyzeDto);
            analyzeDto.setDeviceListId(energyId);
            //查询电能终端下面的所有电表
            List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = tunnelPowerEdgeComputingService.list(Wrappers.lambdaQuery(TunnelPowerEdgeComputing.class).eq(TunnelPowerEdgeComputing::getDevicelistId, energyId));
            List<Long> uniqueIds = tunnelPowerEdgeComputings.stream().map(TunnelPowerEdgeComputing::getUniqueId).collect(Collectors.toList());

            LambdaQueryWrapper<TunnelPowerData> tunnelPowerDataWrapper = Wrappers.lambdaQuery(TunnelPowerData.class);
            tunnelPowerDataWrapper.select(TunnelPowerData::getUniqueId);
            tunnelPowerDataWrapper.eq(TunnelPowerData::getName,"ImpEp");
            tunnelPowerDataWrapper.eq(TunnelPowerData::getDevicelistId,energyId);
            tunnelPowerDataWrapper.ge(TunnelPowerData::getUploadTime,analyzeDto.getStartTime());
            String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
            tunnelPowerDataWrapper.le(TunnelPowerData::getUploadTime,endTime + " 23:59:59");
            List<TunnelPowerData> tunnelPowerDataList = tunnelPowerDataService.list(tunnelPowerDataWrapper);
            List<Long> powerDataUniIds = tunnelPowerDataList.stream().map(TunnelPowerData::getUniqueId).collect(Collectors.toList());
            //判断两个数组之间不包含的数据,如果有，说明采集仪断电了，需要获取上一天的电表数据
            List<Long> onlyIn = uniqueIds.stream()
                    .filter(num -> !powerDataUniIds.contains(num))
                    .collect(Collectors.toList());

            if(!onlyIn.isEmpty()) {
                //获取前天的 本来查16号，但是统计的是15号，15号没值，就查14号
                String startTime = DateUtils.getDay(-1, analyzeDto.getStartTime());
                //获取前天的
                endTime = DateUtils.getDay(-1, endTime);

                QueryWrapper<TunnelPowerData> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("unique_id,MAX(`value`) `value`,MAX(upload_time) upload_time");
                LambdaQueryWrapper<TunnelPowerData> tunnelPowerDataWrapper2 = queryWrapper.lambda();
                tunnelPowerDataWrapper2.eq(TunnelPowerData::getName,"ImpEp");
                tunnelPowerDataWrapper2.in(TunnelPowerData::getUniqueId,onlyIn);
                tunnelPowerDataWrapper2.ge(TunnelPowerData::getUploadTime,startTime);
                tunnelPowerDataWrapper2.le(TunnelPowerData::getUploadTime,endTime + " 23:59:59");
                tunnelPowerDataWrapper2.eq(TunnelPowerData::getDevicelistId,energyId);
                tunnelPowerDataWrapper2.groupBy(TunnelPowerData::getUniqueId);
                tunnelPowerDataWrapper2.orderByDesc(TunnelPowerData::getUploadTime);
                List<TunnelPowerData> tunnelPowerDataListBefore = tunnelPowerDataService.list(tunnelPowerDataWrapper2);

                //然后获取到前天的数据把昨天的数据给赋值上
                List<TunnelPowerData> savePowerData = new ArrayList<>();
                for (TunnelPowerData tunnelPowerData : tunnelPowerDataListBefore) {
                    //这里可以提示昨天断电了
                    TunnelPowerData tunnelPowerData1 = new TunnelPowerData();
                    tunnelPowerData1.setValue(tunnelPowerData.getValue());
                    tunnelPowerData1.setUniqueId(tunnelPowerData.getUniqueId());
                    tunnelPowerData1.setName("ImpEp");
                    tunnelPowerData1.setDevicelistId(energyId);
                    tunnelPowerData1.setUploadTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS,analyzeDto.getStartTime()  + " 00:05:30"));
                    savePowerData.add(tunnelPowerData1);
                }
                tunnelPowerDataService.saveBatch(savePowerData);
            }
        }

    }



    /**
     * 设置查询条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param analyzeDto 参数类
     */
    private void setTime(String startTime, String endTime, AnalyzeDto analyzeDto) throws ParseException {
        if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime) ) {
            analyzeDto.setStartTime(DateUtils.getDay(-1,startTime));
            analyzeDto.setEndTime(endTime);
        }else{
            analyzeDto.setStartTime(DateUtils.getDay(-1,getTodayStartTime()));
            analyzeDto.setEndTime(getTodayEndTime());
        }
    }


    /**
     * @return 当天的零点时间
     */
    public static String getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date time = calendar.getTime();
        return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,time);
    }

    /**
     * @return 当天的末尾时间
     */
    public static String getTodayEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date time = calendar.getTime();
        return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,time);
    }

    public static void main(String[] args) {
        System.out.println(getTodayEndTime());
    }

}
