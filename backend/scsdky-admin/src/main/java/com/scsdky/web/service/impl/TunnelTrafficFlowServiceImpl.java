package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.common.ParamBuild;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.TrafficFlowCountVo;
import com.scsdky.web.domain.vo.TrafficFlowOrSpeedVo;
import com.scsdky.web.domain.vo.monitor.SpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficSpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficVo;
import com.scsdky.web.mapper.TunnelTrafficFlowMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.DateUtils;
import com.scsdky.web.utils.ExportExcelUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author tubo
 */
@Service
public class TunnelTrafficFlowServiceImpl extends ServiceImpl<TunnelTrafficFlowMapper, TunnelTrafficFlow> implements TunnelTrafficFlowService{
    @Resource
    private TunnelNameResultService tunnelNameResultService;
    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelTrafficFlowDayService tunnelTrafficFlowDayService;

    @Resource
    private ParamBuild paramBuild;

    @Override
    public List<TrafficFlowOrSpeedVo> trafficFlowOrSpeed(AnalyzeDto analyzeDto) throws ParseException {
        analyzeDto.setIsHour(0);
        if(analyzeDto.getEndTime() != null && analyzeDto.getStartTime() != null ){
            int day = DateUtils.getDay(analyzeDto.getEndTime(), analyzeDto.getStartTime());
            if(day == 0) {
                analyzeDto.setIsHour(1);
            }else{
                analyzeDto.setIsHour(0);
            }
        }
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //参数构建
        paramBuild.setAnalyzeDto(analyzeDto);
        List<TrafficFlowOrSpeedVo> speedVos = getBaseMapper().trafficFlowOrSpeed(analyzeDto);
        //将所有速度取绝对值
        speedVos.forEach(speedVo -> {
            if (tunnelEdgeComputingTerminal.getNumberOfRepeats() != 0) {
                speedVo.setTrafficFlow(Math.abs(speedVo.getTrafficFlow()) / tunnelEdgeComputingTerminal.getNumberOfRepeats());
            }
            speedVo.setMaxTrafficFlow(Math.abs(speedVo.getMaxTrafficFlow()));
            speedVo.setMinTrafficFlow(Math.abs(speedVo.getMinTrafficFlow()));
            speedVo.setAvgTrafficFlow(Math.abs(speedVo.getAvgTrafficFlow()));
            speedVo.setMaxSpeed(Math.abs(speedVo.getMaxSpeed()));
            speedVo.setMinSpeed(Math.abs(speedVo.getMinSpeed()));
            speedVo.setAvgSpeed(Math.abs(speedVo.getAvgSpeed()));
        });
        return speedVos;
    }

    @Override
    public void exportTraffic(HttpServletResponse response, AnalyzeDto analyzeDto) {
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        // 获取小时车流量
        List<TrafficVo> trafficVos = baseMapper.getHourTraffic(analyzeDto);
        List<TrafficSpeedVo> allSpeedVos = baseMapper.getAllSpeedByQuery(analyzeDto);
        HashMap<String, List<TrafficSpeedVo>> hourSpeeds = new HashMap<>();
        for (TrafficSpeedVo trafficSpeedVo: allSpeedVos) {
            if(hourSpeeds.containsKey(trafficSpeedVo.getUploadHour())){
                //拆分车速，因为车速是由20个车组成的，排除为0的
                String[] speeds = trafficSpeedVo.getSpeed().split(",");
                TrafficSpeedVo trafficSpeedVo1;
                for (String speed : speeds) {
                    if(!"0".equals(speed)) {
                        trafficSpeedVo1 = new TrafficSpeedVo();
                        trafficSpeedVo1.setUploadHour(trafficSpeedVo.getUploadHour());
                        trafficSpeedVo1.setUploadTime(trafficSpeedVo.getUploadTime());
                        trafficSpeedVo1.setSpeed(speed);
                        hourSpeeds.get(trafficSpeedVo.getUploadHour()).add(trafficSpeedVo1);
                    }
                }
            } else {
                List<TrafficSpeedVo> trafficSpeedVos = new ArrayList<>();
                //拆分车速，因为车速是由20个车组成的，排除为0的
                String[] speeds = trafficSpeedVo.getSpeed().split(",");
                TrafficSpeedVo trafficSpeedVo1;
                for (String speed : speeds) {
                    if(!"0".equals(speed)) {
                        trafficSpeedVo1 = new TrafficSpeedVo();
                        trafficSpeedVo1.setUploadHour(trafficSpeedVo.getUploadHour());
                        trafficSpeedVo1.setUploadTime(trafficSpeedVo.getUploadTime());
                        trafficSpeedVo1.setSpeed(speed);
                        trafficSpeedVos.add(trafficSpeedVo1);
                    }
                }
                //如果有值才添加
                if(!trafficSpeedVos.isEmpty()) {
                    hourSpeeds.put(trafficSpeedVo.getUploadHour(),trafficSpeedVos);
                }
            }
        }
        for (TrafficVo trafficVo: trafficVos) {
            if (hourSpeeds.containsKey(trafficVo.getHourUpload())) {
                trafficVo.setTrafficSpeedVoList(hourSpeeds.get(trafficVo.getHourUpload()));
            }
        }
        //根据隧道id 查询编号
        TunnelNameResult tunnelNameResult = tunnelNameResultService.getById(analyzeDto.getTunnelId());
        // 获取隧道名称
        TunnelNameResult tunnelParent = tunnelNameResultService.getById(tunnelNameResult.getParentId());
        // 获取高速公路名称
        TunnelNameResult tunnelRoadParent = tunnelNameResultService.getById(tunnelParent.getParentId());
        String title = tunnelRoadParent.getTunnelName()+tunnelParent.getTunnelName()+"("
                +tunnelNameResult.getTunnelName()+")";
        String dateRange = analyzeDto.getStartTime().replace("-",".")+"-"+analyzeDto.getEndTime().replace("-",".");
        try {
            ExportExcelUtils.trafficFlowOrSpeedExportTemp(response, title, dateRange, trafficVos);
        } catch (Exception e){
            throw new ServiceException("车流车速数据导出失败！");
        }
    }

    @Override
    public List<TrafficVo> clByHouse(AnalyzeDto analyzeDto) {
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        analyzeDto.setEntryTrafficLimit(tunnelEdgeComputingTerminal.getEntryTrafficLimit());
        List<TrafficVo> trafficVos = getBaseMapper().clByHouse(analyzeDto);
        if(CollectionUtils.isNotEmpty(trafficVos)) {
            for (TrafficVo trafficVo : trafficVos) {
                trafficVo.setTrafficFlow(trafficVo.getTrafficFlow() / tunnelEdgeComputingTerminal.getNumberOfRepeats());
                int totalFlow = 0;
                //获取累计车流
                for (TrafficVo trafficVo1 : trafficVos) {
                    if(trafficVo1.getHour() <= trafficVo.getHour()) {
                        totalFlow += trafficVo1.getTrafficFlow();
                    }
                }
                trafficVo.setTotalFlow(totalFlow);
            }
        }
        return trafficVos;
    }

    @Override
    public List<SpeedVo> csByHouse(AnalyzeDto analyzeDto) {
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(analyzeDto.getTunnelId());
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        analyzeDto.setEntrySpeedLimit(tunnelEdgeComputingTerminal.getEntrySpeedLimit());
        //获取五分钟速度
        List<SpeedVo> result = getSpeedVos();
        List<SpeedVo> speedVos = getBaseMapper().csByHouse(analyzeDto);
        if(CollectionUtils.isNotEmpty(speedVos)) {
            for (SpeedVo speedVo : speedVos) {
                for (SpeedVo resultVo : result) {
                    if(speedVo.getHour().equals(resultVo.getHour())) {
                        resultVo.setHour(speedVo.getHour());
                        resultVo.setAvgSpeed(speedVo.getAvgSpeed());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取速度
     * @return
     */
    private List<SpeedVo> getSpeedVos() {
        List<SpeedVo> result = new ArrayList<>();
        //默认构建24小时的数据
        for (int i = 0; i < 24; i++) {
            //每个小时五分钟统计一次，先默认赋值为0
            SpeedVo speedVo;
            for (int j = 0; j < 12; j++) {
                speedVo = new SpeedVo();
                if(i < 10) {
                    speedVo.setHour( "0" + i + ":" + j * 5);
                }else{
                    speedVo.setHour(i + ":" + j * 5);
                }
                speedVo.setAvgSpeed("0");
                result.add(speedVo);
            }

        }
        return result;
    }

    @Override
    public TrafficFlowCountVo getAvgData(AnalyzeDto analyzeDto) {
        return getBaseMapper().getAvgData(analyzeDto);
    }

    @Override
    public List<TrafficFlowOrSpeedVo> trafficFlowOrSpeedV2(AnalyzeDto analyzeDto) {
        return tunnelTrafficFlowDayService.list(new LambdaQueryWrapper<TunnelTrafficFlowDay>()
                .eq(TunnelTrafficFlowDay::getTunnelId, analyzeDto.getTunnelId())
                .ge(StringUtils.isNotBlank(analyzeDto.getStartTime()), TunnelTrafficFlowDay::getUploadTime, analyzeDto.getStartTime())
                .le(StringUtils.isNotBlank(analyzeDto.getEndTime()), TunnelTrafficFlowDay::getUploadTime, analyzeDto.getEndTime())
                .orderByDesc(TunnelTrafficFlowDay::getUploadTime)).stream().map(td -> {
            TrafficFlowOrSpeedVo trafficFlowOrSpeedVo = new TrafficFlowOrSpeedVo();
            BeanUtils.copyProperties(td, trafficFlowOrSpeedVo);
            return trafficFlowOrSpeedVo;
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println(BigDecimal.valueOf(24).subtract(BigDecimal.valueOf(2.70)));
    }
}