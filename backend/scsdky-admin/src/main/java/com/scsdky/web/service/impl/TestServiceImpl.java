//package com.scsdky.web.service.impl;
//
//import com.scsdky.web.domain.*;
//import com.scsdky.web.service.*;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
///**
// * @author tubo
// * gouzaoshuju
// * @date 2023/12/11
// */
//@Component
//public class TestServiceImpl {
//
//    @Resource
//    private TunnelLoopFaultService tunnelLoopFaultService;
//
//    @Resource
//    private TunnelTrafficFlowService tunnelTrafficFlowService;
//
//    @Resource
//    private TunnelLightOutsideService tunnelLightOutsideService;
//
//    @Resource
//    private TunnelStatisticsService tunnelStatisticsService;
//
//    @Resource
//    private TunnelEnergyCarbonService tunnelEnergyCarbonService;
//
//    /**
//     * 回路故障 -每八点
//     */
//    @Scheduled(cron="0 0 8 * * ? ")
//    public void buildLoopFault (){
//        List<String> loopList = new ArrayList<>();
//        loopList.add("R1-01");
//        loopList.add("R1-02");
//        loopList.add("R2-01");
//        loopList.add("R2-02");
//        loopList.add("R2-03");
//        loopList.add("G1-01");
//        loopList.add("G1-02");
//        loopList.add("G2-01");
//        loopList.add("G3-01");
//        loopList.add("J-01");
//        loopList.add("J-02");
//        loopList.add("J-03");
//        loopList.add("C1-01");
//        loopList.add("C2-01");
//        for (int i = 1; i <= 10; i++) {
//            int finalI = i;
//            loopList.forEach(loop -> {
//                TunnelLoopFault tunnelLoopFault = new TunnelLoopFault();
//                tunnelLoopFault.setLoopNumber(loop);
//                tunnelLoopFault.setOccurrenceTime(new Date());
//                tunnelLoopFault.setRecoveryTime(new Date());
//                tunnelLoopFault.setTunnelId((long) finalI);
//                tunnelLoopFault.setTunnelName("玉溪到楚雄");
//                tunnelLoopFault.setZone("入口段1（R1)");
//                tunnelLoopFault.setExpectFailNum(randomNum(100));
//                tunnelLoopFault.setVoltage(randomNum(240));
//                tunnelLoopFault.setElectric(randomNum(60));
//                tunnelLoopFault.setUploadTime(new Date());
//                tunnelLoopFaultService.save(tunnelLoopFault);
//            });
//        }
//    }
//
//
//    /**
//     * 构建车流车速 --每小时
//     */
//    @Scheduled(cron="0 0 * * * ? ")
//    public void buildTrafficFlow (){
//        for (int i = 1; i <= 10; i++) {
//            for (int j = 0; j < 20; j++) {
//                TunnelTrafficFlow trafficFlow = new TunnelTrafficFlow();
//                trafficFlow.setTrafficFlow(randomNum(1000));
//                trafficFlow.setMaxSpeed(randomNum(200));
//                trafficFlow.setMinSpeed(randomNum(60));
//                trafficFlow.setTunnelId((long) i);
//                trafficFlow.setSpeed((randomNum(120)));
//                trafficFlow.setUploadTime(new Date());
//                tunnelTrafficFlowService.save(trafficFlow);
//            }
//        }
//
//    }
//
//    /**
//     * 构建洞内外照度 --每15分钟
//     */
//    @Scheduled(cron="0 0/15 * * * ?")
//    public void buildLightOutside (){
//        for (int i = 1; i <= 10; i++) {
//            TunnelLightOutside tunnelLightOutside = new TunnelLightOutside();
//            tunnelLightOutside.setTunnelId((long) i);
//            Integer lightUpTime = randomNum(24);
//            tunnelLightOutside.setLightUp(lightUpTime);
//            tunnelLightOutside.setLightDown(24 - lightUpTime);
//            tunnelLightOutside.setUploadTime(new Date());
//            Integer num1 = randomNum(4400);
//            Integer num2 = randomNum(100);
//            tunnelLightOutside.setMaxOutside(num1);
//            tunnelLightOutside.setMinOutside(num2);
//            tunnelLightOutside.setAvgOutside(new BigDecimal(num1).divide(new BigDecimal(num2), 2,RoundingMode.HALF_UP));
//            String s1 = String.valueOf(randomNum(80));
//            String s2 = String.valueOf(randomNum(10));
//            tunnelLightOutside.setMaxDimmingRadio(s1);
//            tunnelLightOutside.setMinDimmingRadio(s2);
//            tunnelLightOutside.setOutsideLightValue(randomNum(4400));
//            tunnelLightOutside.setDimmingRadioValue(randomNum(100));
//            tunnelLightOutside.setMinDimmingRadio(s2);
//            tunnelLightOutside.setAvgDimmingRadio(new BigDecimal(s1).divide(new BigDecimal(s2),2, RoundingMode.HALF_UP));
//            tunnelLightOutsideService.save(tunnelLightOutside);
//        }
//
//    }
//
//
//    /**
//     * 构建总体分析 --每2小时推送
//     */
//    @Scheduled(cron="0 0 0/2 * * ? ")
//    public void buildStatistics(){
//        for (int i = 1; i <= 10; i++) {
//            TunnelStatistics tunnelStatistics = new TunnelStatistics();
//            tunnelStatistics.setUploadTime(new Date());
//            tunnelStatistics.setOriginalPowerConsumption(144);
//            tunnelStatistics.setOriginalUnitPowerConsumption(72);
//            tunnelStatistics.setOriginalOperatingPower(6);
//            tunnelStatistics.setOriginalLightUpTime(24);
//            tunnelStatistics.setOriginalCarbonEmission("0.082512");
//            tunnelStatistics.setActualUnitPowerConsumption(randomNum(100));
//            tunnelStatistics.setActualOperatingPower(6);
//            //亮灯时间
//            Integer lightUp = randomNum(24);
//            tunnelStatistics.setActualLightUpTime(lightUp);
//            tunnelStatistics.setActualPowerConsumption(6 * lightUp);
//
//            //实际碳排放量
//            tunnelStatistics.setActualcarbonemission(String.valueOf((6 * lightUp) * 0.573 / 1000));
//            tunnelStatistics.setTunnelId((long)i);
//            tunnelStatisticsService.save(tunnelStatistics);
//        }
//
//    }
//
//
//    /**
//     * 构建能碳数据--每天推送
//     */
//    @Scheduled(cron="0 0 8 * * ? ")
//    public void buildCarbon(){
//        for (int i = 1; i <= 10; i++) {
//            TunnelEnergyCarbon tunnelEnergyCarbon = new TunnelEnergyCarbon();
//            tunnelEnergyCarbon.setTunnelId((long)i);
//            tunnelEnergyCarbon.setUploadTime(new Date());
//            //当日耗电量
//            tunnelEnergyCarbon.setDailyPowerConsumption(randomNum(100));
//            //累计节电量
//            tunnelEnergyCarbon.setTotalPowerSavings(randomNum(100));
//            //累计耗电量
//            tunnelEnergyCarbon.setCumulativePowerConsumption(randomNum(4000));
//            tunnelEnergyCarbon.setRatio("0.23");
//            tunnelEnergyCarbon.setPlantTrees("0.23");
//            tunnelEnergyCarbonService.save(tunnelEnergyCarbon);
//        }
//
//    }
//
//
//
//    public static Integer randomNum(int num){
//        Random random = new Random();
//        int randomNumber = random.nextInt(num) + 1;
//        return randomNumber;
//    }
//}
