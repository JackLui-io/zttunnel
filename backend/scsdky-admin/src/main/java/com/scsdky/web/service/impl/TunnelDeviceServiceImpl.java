package com.scsdky.web.service.impl;

import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deepoove.poi.data.MergeCellRule;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.Tables;
import com.scsdky.common.config.ScsdkyConfig;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.*;
import com.scsdky.web.domain.vo.statistics.StatisticsVo;
import com.scsdky.web.mapper.TunnelDeviceMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.ConvertBit;
import com.scsdky.web.utils.WordUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelDeviceServiceImpl extends ServiceImpl<TunnelDeviceMapper, TunnelDevice> implements TunnelDeviceService {

    private static final Logger log = LoggerFactory.getLogger(TunnelDeviceServiceImpl.class);

    @Resource
    private ScsdkyConfig scsdkyConfig;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelStatisticsService tunnelStatisticsService;

    @Resource
    private TunnelTrafficFlowService tunnelTrafficFlowService;

    @Resource
    private TunnelLightOutsideService tunnelLightOutsideService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelOutOfRadarService tunnelOutOfRadarService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Override
    public List<TunnelDevice> getDeviceListByPage(DeviceDto deviceDto) {
        if("全选".equals(deviceDto.getZone())) {
            deviceDto.setZone("");
        }
        if("全选".equals(deviceDto.getDeviceType())) {
            deviceDto.setDeviceType("");
        }
        return this.getBaseMapper().getDeviceListByPage(deviceDto);
    }


    @Override
    public DeviceTypeVo countByTunnelId(Integer tunnelId) {
        DeviceTypeVo deviceTypeVo = this.getBaseMapper().countByTunnelId(tunnelId);
        return deviceTypeVo;
    }

    @Override
    public DeviceTypeVo countByTunnelIds(List<Long> tunnelIds) {
        if (CollectionUtils.isEmpty(tunnelIds)) {
            DeviceTypeVo vo = new DeviceTypeVo();
            vo.setDeviceCount(0L);
            vo.setDeviceOnline(0L);
            vo.setDeviceOffline(0L);
            vo.setDeviceBreakdown(0L);
            return vo;
        }
        DeviceTypeVo vo = this.getBaseMapper().countByTunnelIds(tunnelIds);
        return vo != null ? vo : new DeviceTypeVo();
    }

    @Override
    public List<String> getDeviceStatus() {
        List<TunnelDevice> tunnelDeviceList = list(new LambdaQueryWrapper<TunnelDevice>().groupBy(TunnelDevice::getDeviceStatus));
        List<String> statusList = tunnelDeviceList.stream().map(TunnelDevice::getDeviceStatus).collect(Collectors.toList());
        return statusList;
    }

    @Override
    public List<String> getDeviceType() {
        List<TunnelDevice> tunnelDeviceList = list(new LambdaQueryWrapper<TunnelDevice>().groupBy(TunnelDevice::getDeviceType));
        List<String> typeList = tunnelDeviceList.stream().map(TunnelDevice::getDeviceType).collect(Collectors.toList());
        return typeList;
    }

    @Override
    public Map<String, Object> mileageInfo(AnalyzeDto analyzeDto) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        //统计vo
        ReportVo reportVo = new ReportVo();
        //隧道信息具体值
        List<Map<String, Object>> result = new ArrayList<>();
        //获取在线设备的信息
        List<TunnelDevice> deviceList = list(new LambdaQueryWrapper<TunnelDevice>().eq(TunnelDevice::getDeviceStatus, "00")
                .eq(TunnelDevice::getTunnelId, analyzeDto.getTunnelId())
                .between(analyzeDto.getStartTime() != null, TunnelDevice::getCreateTime, analyzeDto.getStartTime(), analyzeDto.getEndTime()));
        if (CollectionUtils.isNotEmpty(deviceList)) {
            TunnelNameResult tunnelName = tunnelNameResultService.getById(analyzeDto.getTunnelId());
            //查询上级
            TunnelNameResult topTunnel = tunnelNameResultService.getById(tunnelName.getParentId());
            for (TunnelDevice tunnelDevice : deviceList) {
                Map<String, Object> param = new HashMap<>();
                param.put("tunnelName", tunnelName.getTunnelName());
                param.put("mileage", 100L);
                param.put("deviceNum", tunnelDevice.getDeviceNum());
                param.put("onLineTime", tunnelDevice.getUpdateTime());
                result.add(param);
            }
            reportVo.setTunnelName(topTunnel.getTunnelName() + "(" + tunnelName.getTunnelName() + ")");
            //获取倒数第二集公路路线级
            String name = "";
            TunnelNameResult secondTunnelName = getSecondTunnelName(topTunnel, name);
            reportVo.setRoute(secondTunnelName.getTunnelName());
            reportVo.setMileage(secondTunnelName.getTunnelMileage());

        }
        reportVo.setList(result);
        map.put("gcgk", reportVo);

        //工程概算
        reportVo = new ReportVo();
        //隧道信息具体值
        result = new ArrayList<>();
        //获取设备类型的信息
        int deviceNum = 0;
        //设备健康程度
        BigDecimal healty = null;
        //在线数
        int onlineNum = 0;
        List<DeviceTypeAndStatusVo> deviceGroupByTypeAndStatus = this.getBaseMapper().getDeviceGroupByTypeAndStatus(analyzeDto);
        if (CollectionUtils.isNotEmpty(deviceGroupByTypeAndStatus)) {
            TunnelNameResult tunnelName = tunnelNameResultService.getById(analyzeDto.getTunnelId());
            //查询上级
            TunnelNameResult topTunnel = tunnelNameResultService.getById(tunnelName.getParentId());
            for (DeviceTypeAndStatusVo deviceTypeAndStatusVo : deviceGroupByTypeAndStatus) {
                Map<String, Object> param = new HashMap<>();
                param.put("tunnelName", tunnelName.getTunnelName() );
                param.put("deviceType", deviceTypeAndStatusVo.getDeviceType());
                param.put("deviceOnline", deviceTypeAndStatusVo.getDeviceOnline());
                param.put("deviceOffline", deviceTypeAndStatusVo.getDeviceOffline());
                param.put("deviceBreakdown", deviceTypeAndStatusVo.getDeviceBreakdown());
                result.add(param);
                deviceNum+=deviceTypeAndStatusVo.getNum();
                onlineNum+=deviceTypeAndStatusVo.getDeviceOnline();
            }
            healty = new BigDecimal(onlineNum).divide(BigDecimal.valueOf(result.size()), 2, RoundingMode.HALF_UP);
            reportVo.setTunnelName(topTunnel.getTunnelName() + "(" + tunnelName.getTunnelName() + ")");
            //获取倒数第二集公路路线级
            String name = "";
            TunnelNameResult secondTunnelName = getSecondTunnelName(topTunnel, name);
            reportVo.setRoute(secondTunnelName.getTunnelName());
        }
        reportVo.setList(result);
        map.put("sbgl", reportVo);

        //数据统计
        reportVo = new ReportVo();
        //隧道信息具体值
        result = new ArrayList<>();
        //获取统计分析内容
        List<StatisticsVo> statistics = tunnelStatisticsService.statistics(analyzeDto);
        //车流车速
        TrafficFlowCountVo trafficFlow = tunnelTrafficFlowService.getAvgData(analyzeDto);

        //洞内外照度--后面需要更改先把数据弄出来，后期是具体的值，通过具体的值去算平均值
        LightCountVo lightCountVo = tunnelLightOutsideService.getAvgData(analyzeDto);

        //节约电量
        String rate=null;
        //理论节电率
        BigDecimal theoreticalRateRadio = null;
        //碳减排量
        BigDecimal tzpl = null;
        if (CollectionUtils.isNotEmpty(statistics)) {
            TunnelNameResult tunnelName = tunnelNameResultService.getById(analyzeDto.getTunnelId());
            //查询上级
            TunnelNameResult topTunnel = tunnelNameResultService.getById(tunnelName.getParentId());
            for (StatisticsVo statisticsVo : statistics) {
                Map<String, Object> param = new HashMap<>();
                param.put("tunnelName", tunnelName.getTunnelName());
                param.put("theoreticalPowerSavingRate", statisticsVo.getTheoreticalPowerSavingRate());
                param.put("theoreticalCarbonEmissionReduction", statisticsVo.getTheoreticalOperatingPowerReduction());
                param.put("theoreticalTotalPowerReduction", statisticsVo.getTheoreticalOperatingPowerReduction());
                param.put("theoreticalLightUpTimeReduction", statisticsVo.getTheoreticalLightUpTimeReduction());
                param.put("avgTrafficFlow", trafficFlow == null ? 0:trafficFlow.getTrafficFlow());
                param.put("avgSpeed", trafficFlow == null ? 0:trafficFlow.getAvgSpeed());
                param.put("avgOutside", lightCountVo == null ? 0:lightCountVo.getAvgOutside());
                param.put("avgLight", lightCountVo == null ?0:lightCountVo.getAvgLight());
                param.put("avgDimmingRadio", 13.00);
                result.add(param);
                rate = String.valueOf(statisticsVo.getOriginalPowerConsumption() - statisticsVo.getActualPowerConsumption());
                tzpl = statisticsVo.getOriginalCarbonEmission().subtract(statisticsVo.getActualCarbonEmission());
                theoreticalRateRadio = new BigDecimal(rate).divide(BigDecimal.valueOf(statisticsVo.getOriginalPowerConsumption()), 2, RoundingMode.FLOOR);
            }
            reportVo.setTunnelName(topTunnel.getTunnelName() + "(" + tunnelName.getTunnelName() + ")");
            //获取倒数第二集公路路线级
            String name = "";
            TunnelNameResult secondTunnelName = getSecondTunnelName(topTunnel, name);
            reportVo.setRoute(secondTunnelName.getTunnelName());

        }
        reportVo.setList(result);
        map.put("sjtj", reportVo);

        //异常事件
        reportVo = new ReportVo();
        //隧道信息具体值
        result = new ArrayList<>();
        List<DeviceTypeAndStatusVo> deviceGroupByTypeAndStatus1 = this.getBaseMapper().getDeviceGroupByTypeAndStatus(analyzeDto);
        List<String> deviceTypeList = deviceGroupByTypeAndStatus1.stream().map(DeviceTypeAndStatusVo::getDeviceType).collect(Collectors.toList());
        //设备类型
        reportVo.setDeviceTypeList(deviceTypeList);

        Map<String, Object> deviceTypeValue = new HashMap<>();
        deviceGroupByTypeAndStatus1.forEach(device -> {
            deviceTypeValue.put(device.getDeviceType(),device.getDeviceOffline());
        });

        result.add(deviceTypeValue);
        if (CollectionUtils.isNotEmpty(deviceGroupByTypeAndStatus1)) {
            TunnelNameResult tunnelName = tunnelNameResultService.getById(analyzeDto.getTunnelId());
            //查询上级
            TunnelNameResult topTunnel = tunnelNameResultService.getById(tunnelName.getParentId());
            //获取倒数第二集公路路线级
            String name = "";
            TunnelNameResult secondTunnelName = getSecondTunnelName(topTunnel, name);
            reportVo.setTunnelName(topTunnel.getTunnelName() + "(" +tunnelName.getTunnelName() + ")");

            reportVo.setRoute(secondTunnelName.getTunnelName());

        }
        reportVo.setList(result);
        map.put("ycsj", reportVo);

        if(healty == null ){
            healty = new BigDecimal(0);
        }
        if(StringUtils.isBlank(rate) ){
            rate = "0";
        }
        double v = Double.parseDouble(rate) * 0.6 * 0.0001;
        String  s1 = String.format("%.2f",v);
        double v1 = Double.parseDouble(rate) * 0.573 * 0.001;
        String  s2 = String.format("%.2f",v1);
        String sb = "该路段" + analyzeDto.getStartTime() + "到" + analyzeDto.getEndTime() + "理论节电量" + rate + "kwh," + "理论节电率为" +
                theoreticalRateRadio + "%," + "减少成本支出" + s1 + "万元," + "减少二氧化碳减排量" +
                s2 + "t。" + "\n" + "该路段共计设备" + deviceNum + "台" +
                "，健康程度" + healty.multiply(new BigDecimal(100)) + "%" + "，预计下次预计维护时间为2024年01月13日。";
        map.put("end", sb);
        return map;

    }


    private TunnelNameResult getSecondTunnelName(TunnelNameResult tunnelName,String name){
        TunnelNameResult topTunnel = tunnelNameResultService.getById(tunnelName.getParentId());
        return topTunnel;
    }

    @Override
    public void getWord(HttpServletResponse response,AnalyzeDto analyzeDto) throws IOException, ParseException {
        //存放word所需要的数据
        Map<String, Object> dates = new HashMap<>();
        //构建模板数据
        dates.put("startYear",analyzeDto.getStartTime());
        dates.put("endYear",analyzeDto.getEndTime());
        dates.put("year", com.scsdky.common.utils.DateUtils.getYear());
        dates.put("month", com.scsdky.common.utils.DateUtils.getMonth());
        dates.put("day", com.scsdky.common.utils.DateUtils.getDay());


        //查询隧道基本信息--工程概况
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        dates.put("lineName",tunnelEdgeComputingTerminal.getLineName());
        dates.put("lineMileage",tunnelEdgeComputingTerminal.getLineMileage());
        dates.put("tunnelName",tunnelEdgeComputingTerminal.getTunnelName() + tunnelEdgeComputingTerminal.getDirection());
        dates.put("lineMileageTunnel",tunnelEdgeComputingTerminal.getTunnelMileage());
        dates.put("onlineTime", DateUtils.format(tunnelEdgeComputingTerminal.getCreateTime(),"yyyy-MM-dd"));

        //设备及状态情况

        //构建表格数据
        List<RowRenderData> style = new ArrayList<>();
        RowRenderData row0 = Rows.of("设备类别", "设备号","安装位置","设备状态").textColor("000000").textBold()
                .textFontSize(12)
                .center().create();
        style.add(row0);

        //边缘控制器
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setTunnelId(analyzeDto.getTunnelId());
        deviceDto.setType(1);
        List<TunnelDevicelistVo> devicelist = tunnelDevicelistService.getDevicelist(deviceDto);
        //电能终端
        deviceDto.setType(2);
        List<TunnelDevicelistVo> devicePowerlist = tunnelDevicelistService.getDevicelist(deviceDto);
        devicelist.addAll(devicePowerlist);

        //边缘和电能渲染
        for (TunnelDevicelistVo tunnelDevicelist : devicelist) {
            //将数值转为大桩号+小桩号
            Integer deviceNum = tunnelDevicelist.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);
            RowRenderData row1 = Rows.of(tunnelDevicelist.getNickName(), String.valueOf(tunnelDevicelist.getDeviceId()),
                    (bigAndSmall),tunnelDevicelist.getOnline() == 1 ? "在线":"离线").textColor("000000")
                    .textFontSize(12)
                    .center().create();
            style.add(row1);
        }

        deviceDto.setType(1);
        List<TunnelOutOfRadar> deviceDwlds = tunnelOutOfRadarService.getDeviceDwld(deviceDto);
        deviceDto.setType(2);
        deviceDwlds.addAll(tunnelOutOfRadarService.getDeviceDwld(deviceDto));
        //洞外雷达和洞外亮度仪
        for (TunnelOutOfRadar deviceDwld : deviceDwlds) {
            //将数值转为大桩号+小桩号
            Integer deviceNum = deviceDwld.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);
            if(deviceDwld.getType() == 1) {
                RowRenderData row1 = Rows.of("洞外雷达", String.valueOf(deviceDwld.getDeviceId()),
                        (bigAndSmall), "00".equals(deviceDwld.getDeviceStatus()) ? "正常":"异常").textColor("000000")
                        .textFontSize(12)
                        .center().create();
                style.add(row1);
            }else {
                RowRenderData row1 = Rows.of("洞外亮度仪", String.valueOf(deviceDwld.getDeviceId()),
                        (bigAndSmall), "00".equals(deviceDwld.getDeviceStatus()) ? "正常":"异常").textColor("000000")
                        .textFontSize(12)
                        .center().create();
                style.add(row1);
            }
        }

        //合并单元格需要的类
        MergeCellRule.MergeCellRuleBuilder mergeCellRuleBuilder = MergeCellRule.builder();

        //灯具终端
        List<TunnelLampsTerminal> deviceLamps = tunnelLampsTerminalService.getDeviceLamp(deviceDto);

        for (TunnelLampsTerminal deviceLamp : deviceLamps) {
            //将数值转为大桩号+小桩号
            Integer deviceNum = deviceLamp.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);

            RowRenderData row1 = Rows.of("灯具终端", String.valueOf(deviceLamp.getDeviceId()),
                    (bigAndSmall),"通信状态:" + deviceLamp.getCommunicationStateStr() + "\n" + "工作状态:" + deviceLamp.getWorkStateStr() + "\n" + "雷达状态:" + deviceLamp.getLdStatusName() ).textColor("000000")
                    .textFontSize(12)
                    .center().create();
            style.add(row1);
        }

        // 检查是否有数据行（除了表头）
        if (style.size() > 1) {
            // 只有表头时不需要合并单元格
            int totalRows = devicelist.size() + deviceDwlds.size() + deviceLamps.size();
            if (totalRows > 0) {
                // 计算合并单元格的范围：从第一行数据（索引1）到最后一行数据
                int startRow = devicelist.size() + deviceDwlds.size() + 1; // 灯具终端开始的行
                int endRow = devicelist.size() + deviceDwlds.size() + deviceLamps.size(); // 灯具终端结束的行
                if (startRow <= endRow && endRow > 0) {
                    mergeCellRuleBuilder.map(MergeCellRule.Grid.of(startRow, 0), MergeCellRule.Grid.of(endRow, 0));
                }
            }
        }
        MergeCellRule tableRule = mergeCellRuleBuilder.build();

        double[] width = {2.94d,4.57d,3.76d,3.76d};
        Tables.TableBuilder tableBuilder = Tables.ofAutoWidth().width(15.03,width).center();
        for (RowRenderData rowRenderData : style) {
            tableBuilder.addRow(rowRenderData);
        }
        
        // 记录表格数据信息用于调试
        log.info("表格数据行数（包含表头）：{}", style.size());
        log.info("边缘控制器和电能终端数量：{}", devicelist.size());
        log.info("洞外雷达和亮度仪数量：{}", deviceDwlds.size());
        log.info("灯具终端数量：{}", deviceLamps.size());
        
        dates.put("table",tableBuilder.mergeRule(tableRule).create());

        //第三部分，节能分析
        //车流
        List<TrafficFlowOrSpeedVo> speedVos = tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto);
        //车流总计
        int trafficFlowSum = speedVos.stream().mapToInt(TrafficFlowOrSpeedVo::getTrafficFlow).sum();
        //平均速度总计
        int speedSum = speedVos.stream().mapToInt(TrafficFlowOrSpeedVo::getAvgSpeed).sum();
        if (speedVos.isEmpty()) {
            dates.put("avgDayTrafficFlow", 0);
            dates.put("avgDaySpeed", 0);
        } else {
            dates.put("avgDayTrafficFlow", trafficFlowSum / speedVos.size());
            dates.put("avgDaySpeed", speedSum / speedVos.size());
        }

        //洞外亮度
        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutside(analyzeDto);
        //>3000 最大值
        long oneSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> insideAndOutsideVo.getMaxOutside() > 3000).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //2000-3000
        long twoSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> (insideAndOutsideVo.getMaxOutside() > 2000 && insideAndOutsideVo.getMaxOutside() < 3000)).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //1000-2000
        long threeSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> (insideAndOutsideVo.getMaxOutside() > 1000 && insideAndOutsideVo.getMaxOutside() < 2000)).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //<1000
        long fourSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> insideAndOutsideVo.getMaxOutside() < 1000).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        dates.put("oneSum",oneSum);
        dates.put("twoSum",twoSum);
        dates.put("threeSum",threeSum);
        dates.put("fourSum",fourSum);

        //耗能及节能
        List<StatisticsVo> statistics = tunnelStatisticsService.statistics(analyzeDto);

        if(CollectionUtils.isNotEmpty(statistics)) {
            StatisticsVo statisticsVo = statistics.get(0);
            dates.put("actualPowerConsumption",statisticsVo.getActualPowerConsumption());
            dates.put("theoreticalPowerSavingRate",statisticsVo.getTheoreticalPowerSavingRate() + "%");
            dates.put("actualOperatingPower",statisticsVo.getActualOperatingPower());
            dates.put("theoreticalTotalPowerReduction",statisticsVo.getTheoreticalTotalPowerReduction() + "%");
            dates.put("actualLightUpTime",statisticsVo.getActualLightUpTime());
            dates.put("theoreticalLightUpTimeReduction",statisticsVo.getTheoreticalLightUpTimeReduction() + "%");
            dates.put("actualCarbonEmission",statisticsVo.getActualCarbonEmission());
            dates.put("theoreticalCarbonEmissionReduction",statisticsVo.getTheoreticalCarbonEmissionReduction() + "%");
        }

        // ========== 原代码（已注释）==========
        // 原代码使用硬编码的 Windows 路径，在 Linux 服务器上会失败
        // String templatePath = "C:/report/隧道报表.docx";
        // String fileDir = "C:/report/template";
        
        // ========== 新代码 ==========
        // 使用配置的 profile 路径，支持 Windows 和 Linux
        // 模板文件应该放在 profile 目录下的 report 子目录中
        String profile = scsdkyConfig.getProfile();
        if (StringUtils.isEmpty(profile)) {
            log.error("Word 模板路径配置为空，请检查 scsdky.profile 配置");
            throw new IOException("Word 模板路径配置为空");
        }
        
        // 构建模板路径：profile/report/隧道报表.docx
        String templatePath;
        String fileDir;
        if (profile.endsWith("/") || profile.endsWith("\\")) {
            templatePath = profile + "report/隧道报表.docx";
            fileDir = profile + "report/template";
        } else {
            templatePath = profile + File.separator + "report" + File.separator + "隧道报表.docx";
            fileDir = profile + File.separator + "report" + File.separator + "template";
        }
        
        // 检查模板文件是否存在
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            log.error("Word 模板文件不存在：{}", templatePath);
            log.error("请确保模板文件存在于：{}", templatePath);
            throw new IOException("Word 模板文件不存在：" + templatePath);
        }
        
        String fileName = "报表";
        log.info("开始生成 Word 文档，模板路径：{}", templatePath);
        log.info("表格数据行数：{}", style.size());
        
        String wordPath = WordUtil.createWord(templatePath, fileDir, fileName, dates);
        
        if (StringUtils.isEmpty(wordPath)) {
            log.error("Word 文档生成失败");
            throw new IOException("Word 文档生成失败");
        }
        
        log.info("Word 文档生成成功，路径：{}", wordPath);
        
        // 检查生成的文件是否存在
        File wordFile = new File(wordPath);
        if (!wordFile.exists()) {
            log.error("生成的 Word 文件不存在：{}", wordPath);
            throw new IOException("生成的 Word 文件不存在：" + wordPath);
        }
        
        // 设置响应头，支持中文文件名
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + 
            java.net.URLEncoder.encode("分析报告.docx", "UTF-8"));
        
        FileInputStream is = new FileInputStream(wordPath);
        //下载到客户端
        ServletOutputStream outputStream = response.getOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = is.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        // 关流顺序，先打开的后关闭
        outputStream.close();
        is.close();
        
        log.info("Word 文档下载完成");

    }

    @Override
    public Map<String, Object> reportData(AnalyzeDto analyzeDto) throws ParseException {
        //存放word所需要的数据
        Map<String, Object> dates = new HashMap<>();
        //构建模板数据
        dates.put("startYear",analyzeDto.getStartTime());
        dates.put("endYear",analyzeDto.getEndTime());
        dates.put("year", com.scsdky.common.utils.DateUtils.getYear());
        dates.put("month", com.scsdky.common.utils.DateUtils.getMonth());
        dates.put("day", com.scsdky.common.utils.DateUtils.getDay());


        //查询隧道基本信息--工程概况
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        dates.put("lineName",tunnelEdgeComputingTerminal.getLineName());
        dates.put("lineMileage",tunnelEdgeComputingTerminal.getLineMileage());
        dates.put("tunnelName",tunnelEdgeComputingTerminal.getTunnelName() + tunnelEdgeComputingTerminal.getDirection());
        dates.put("lineMileageTunnel",tunnelEdgeComputingTerminal.getTunnelMileage());
        dates.put("onlineTime", DateUtils.format(tunnelEdgeComputingTerminal.getCreateTime(),"yyyy-MM-dd"));

        //设备基本情况信息表啊
        List<DeviceStatusLocalVo> deviceStatusLocalVos = new ArrayList<>();
        //设备及状态情况
        //边缘控制器
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setTunnelId(analyzeDto.getTunnelId());
        deviceDto.setType(1);
        List<TunnelDevicelistVo> devicelist = tunnelDevicelistService.getDevicelist(deviceDto);
        //电能终端
        deviceDto.setType(2);
        List<TunnelDevicelistVo> devicePowerlist = tunnelDevicelistService.getDevicelist(deviceDto);
        devicelist.addAll(devicePowerlist);

        devicelist.forEach(device -> {
            // 控制验证：检查设备对象是否为空
            if (device == null) {
                log.warn("设备对象为空，跳过处理");
                return;
            }
            
            // 控制验证：检查设备桩号是否为空
            Integer deviceNum = device.getDeviceNum();
            if (deviceNum == null) {
                log.warn("设备ID: {} 的设备桩号为空，跳过处理", device.getDeviceId());
                return;
            }
            
            //将数值转为大桩号+小桩号
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);
            
            // 控制验证：检查转换结果是否为空
            if (StringUtils.isEmpty(bigAndSmall)) {
                log.warn("设备ID: {} 的安装位置转换结果为空，跳过处理", device.getDeviceId());
                return;
            }
            
            // 控制验证：检查设备名称是否为空
            String nickName = device.getNickName();
            if (StringUtils.isEmpty(nickName)) {
                log.warn("设备ID: {} 的设备名称为空，使用默认值", device.getDeviceId());
                nickName = "未知设备";
            }
            
            // 控制验证：检查设备ID是否为空
            Long deviceId = device.getDeviceId();
            if (deviceId == null) {
                log.warn("设备ID为空，跳过处理");
                return;
            }
            
            DeviceStatusLocalVo deviceStatusLocalVo = new DeviceStatusLocalVo();
            deviceStatusLocalVo.setDeviceType(nickName);
            deviceStatusLocalVo.setDeviceId(deviceId);
            deviceStatusLocalVo.setInstallationPosition(bigAndSmall);
            deviceStatusLocalVo.setDeviceStatus(device.getOnline() == 1 ? "在线":"离线");
            deviceStatusLocalVos.add(deviceStatusLocalVo);
        });


        deviceDto.setType(1);
        List<TunnelOutOfRadar> deviceDwlds = tunnelOutOfRadarService.getDeviceDwld(deviceDto);
        deviceDto.setType(2);
        deviceDwlds.addAll(tunnelOutOfRadarService.getDeviceDwld(deviceDto));

        deviceDwlds.forEach(deviceDwld -> {
            //将数值转为大桩号+小桩号
            Integer deviceNum = deviceDwld.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);
            if(deviceDwld.getType() == 1) {
                DeviceStatusLocalVo deviceStatusLocalVo = new DeviceStatusLocalVo();
                deviceStatusLocalVo.setDeviceType("洞外雷达");
                deviceStatusLocalVo.setDeviceId(deviceDwld.getDeviceId());
                deviceStatusLocalVo.setInstallationPosition((bigAndSmall));
                deviceStatusLocalVo.setDeviceStatus("00".equals(deviceDwld.getDeviceStatus()) ? "正常":"异常");
                deviceStatusLocalVos.add(deviceStatusLocalVo);
            }else {
                DeviceStatusLocalVo deviceStatusLocalVo = new DeviceStatusLocalVo();
                deviceStatusLocalVo.setDeviceType("洞外亮度仪");
                deviceStatusLocalVo.setDeviceId(deviceDwld.getDeviceId());
                deviceStatusLocalVo.setInstallationPosition((bigAndSmall));
                deviceStatusLocalVo.setDeviceStatus("00".equals(deviceDwld.getDeviceStatus()) ? "正常":"异常");
                deviceStatusLocalVos.add(deviceStatusLocalVo);
            }
        });

        //灯具终端
        List<TunnelLampsTerminal> deviceLamps = tunnelLampsTerminalService.getDeviceLamp(deviceDto);

        for (TunnelLampsTerminal deviceLamp : deviceLamps) {
            //将数值转为大桩号+小桩号
            Integer deviceNum = deviceLamp.getDeviceNum();
            String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);

            DeviceStatusLocalVo deviceStatusLocalVo = new DeviceStatusLocalVo();
            deviceStatusLocalVo.setDeviceType("灯具终端");
            deviceStatusLocalVo.setDeviceId(deviceLamp.getDeviceId());
            deviceStatusLocalVo.setInstallationPosition((bigAndSmall));
            deviceStatusLocalVo.setDeviceStatus("通信状态:" + deviceLamp.getCommunicationStateStr() + "\n" + "工作状态:" + deviceLamp.getWorkStateStr() + "\n" + "雷达状态:" + deviceLamp.getLdStatusName());
            deviceStatusLocalVos.add(deviceStatusLocalVo);
        }

        dates.put("deviceStatus",deviceStatusLocalVos);
        //第三部分，节能分析
        //车流
        List<TrafficFlowOrSpeedVo> speedVos = tunnelTrafficFlowService.trafficFlowOrSpeed(analyzeDto);
        //车流总计
        int trafficFlowSum = speedVos.stream().mapToInt(TrafficFlowOrSpeedVo::getTrafficFlow).sum();
        //平均速度总计
        int speedSum = speedVos.stream().mapToInt(TrafficFlowOrSpeedVo::getAvgSpeed).sum();
        if (speedSum == 0) {
            dates.put("avgDayTrafficFlow",0);
            dates.put("avgDaySpeed",0);
        }else{
            dates.put("avgDayTrafficFlow",trafficFlowSum / speedVos.size());
            dates.put("avgDaySpeed",speedSum / speedVos.size());
        }

        //洞外亮度
        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelLightOutsideService.insideAndOutside(analyzeDto);
        //>3000 最大值
        long oneSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> insideAndOutsideVo.getMaxOutside() > 3000).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //2000-3000
        long twoSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> (insideAndOutsideVo.getMaxOutside() > 2000 && insideAndOutsideVo.getMaxOutside() < 3000)).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //1000-2000
        long threeSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> (insideAndOutsideVo.getMaxOutside() > 1000 && insideAndOutsideVo.getMaxOutside() < 2000)).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        //<1000
        long fourSum = insideAndOutsideVos.stream().filter(insideAndOutsideVo -> insideAndOutsideVo.getMaxOutside() < 1000).mapToInt(InsideAndOutsideVo::getMaxOutside).count();
        dates.put("oneSum",oneSum);
        dates.put("twoSum",twoSum);
        dates.put("threeSum",threeSum);
        dates.put("fourSum",fourSum);

        //耗能及节能
        List<StatisticsVo> statistics = tunnelStatisticsService.statistics(analyzeDto);

        if(CollectionUtils.isNotEmpty(statistics)) {
            StatisticsVo statisticsVo = statistics.get(0);
            dates.put("actualPowerConsumption",statisticsVo.getActualPowerConsumption());
            dates.put("theoreticalPowerSavingRate",statisticsVo.getTheoreticalPowerSavingRate() + "%");
            dates.put("actualOperatingPower",statisticsVo.getActualOperatingPower());
            dates.put("theoreticalTotalPowerReduction",statisticsVo.getTheoreticalTotalPowerReduction() + "%");
            dates.put("actualLightUpTime",statisticsVo.getActualLightUpTime());
            dates.put("theoreticalLightUpTimeReduction",statisticsVo.getTheoreticalLightUpTimeReduction() + "%");
            dates.put("actualCarbonEmission",statisticsVo.getActualCarbonEmission());
            dates.put("theoreticalCarbonEmissionReduction",statisticsVo.getTheoreticalCarbonEmissionReduction() + "%");
        }
        return dates;
    }
}




