package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.common.ParamBuild;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelInsideOutsideDay;
import com.scsdky.web.domain.TunnelLightOutside;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.LightCountVo;
import com.scsdky.web.domain.vo.monitor.LightOutsideVo;
import com.scsdky.web.mapper.TunnelLightOutsideMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.DateUtils;
import com.scsdky.web.utils.ExportExcelUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelLightOutsideServiceImpl extends ServiceImpl<TunnelLightOutsideMapper, TunnelLightOutside> implements TunnelLightOutsideService{
    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;

    @Resource
    private TunnelInsideOutsideDayService tunnelInsideOutsideDayService;

    @Resource
    private ParamBuild paramBuild;

    public static void main(String[] args) throws ParseException {
        System.out.println(DateUtils.getDay(-1, "2025-04-13"));
    }
    @Override
    public List<InsideAndOutsideVo> insideAndOutside(AnalyzeDto analyzeDto) throws ParseException {
        analyzeDto.setIsHour(0);
        DecimalFormat df = new DecimalFormat("0.00");
        if(analyzeDto.getEndTime() != null && analyzeDto.getStartTime() != null ){
            int day = DateUtils.getDay(analyzeDto.getEndTime(), analyzeDto.getStartTime());
            if(day == 0) {
                analyzeDto.setIsHour(1);
            }else{
                analyzeDto.setIsHour(0);
            }
        }
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        paramBuild.setAnalyzeDto(analyzeDto);
        List<InsideAndOutsideVo> insideAndOutsideVos = baseMapper.insideAndOutside(analyzeDto);

        //查询这个时间段的亮灯时间，按每天的小时统计
        List<Map<String,Object>> lightUpTime = tunnelEdgeComputeDataService.getLightTime(analyzeDto);


        insideAndOutsideVos.forEach(insideAndOutsideVo -> {
            //实际亮灯时间--通过上传数据表tunnel_edge_compute_data表中去获取实际亮灯时间
//            analyzeDto1.setTime(insideAndOutsideVo.getUploadTime());
//            analyzeDto1.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
//            List<Integer> lightUpTime = tunnelEdgeComputeDataService.getLightTime(analyzeDto1);
//            double actualLightUpTime = lightUpTime.stream().mapToInt(Integer::intValue).sum() * 0.1;

            double actualLightUpTime = lightUpTime.stream()
                    .filter(map -> map.get("upload_time").toString().equals(insideAndOutsideVo.getUploadTime())).collect(Collectors.toList())
                    .stream()
                    .mapToInt(map -> (Integer) map.get("lighting_time")).sum() * 0.1;
            BigDecimal hours = new BigDecimal(df.format(actualLightUpTime));
            BigDecimal subtract = BigDecimal.valueOf(24).subtract(new BigDecimal(actualLightUpTime));
            insideAndOutsideVo.setLightRadio(hours.toString() + ":" + df.format(subtract.doubleValue()));
            insideAndOutsideVo.setLightUp(hours.toString());
            insideAndOutsideVo.setLightDown(df.format(24.0 - hours.doubleValue()));
        });
        return insideAndOutsideVos;
    }

    @Override
    public void exportLightOutside(HttpServletResponse response, AnalyzeDto analyzeDto) {
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        analyzeDto.setIsHour(2);
        List<InsideAndOutsideVo> insideAndOutsideVos = baseMapper.insideAndOutside(analyzeDto);
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
            ExportExcelUtils.insideAndOutsideExportTemp(response, title, dateRange, insideAndOutsideVos);
        } catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("洞外亮度数据导出失败！");
        }
    }

    @Override
    public List<LightOutsideVo> zdByHouse(AnalyzeDto analyzeDto) {
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        List<LightOutsideVo> result = new ArrayList<>();
        //默认构建24小时的数据
        for (int i = 0; i < 24; i++) {
            //每个小时五分钟统计一次，先默认赋值为0
            LightOutsideVo lightOutsideVo;
            for (int j = 0; j < 12; j++) {
                lightOutsideVo = new LightOutsideVo();
                if(i < 10) {
                    lightOutsideVo.setHour( "0" + i + ":" + j * 5);
                }else{
                    lightOutsideVo.setHour(i + ":" + j * 5);
                }
                lightOutsideVo.setOutside(BigDecimal.ZERO);
                lightOutsideVo.setDimmingRadio(BigDecimal.ZERO);
                result.add(lightOutsideVo);
            }

        }

        List<LightOutsideVo> lightOutsideVos = getBaseMapper().zdByHouse(analyzeDto);
        if(CollectionUtils.isNotEmpty(lightOutsideVos)) {
            for (LightOutsideVo lightOutsideVo : lightOutsideVos) {
                for (LightOutsideVo resultVo : result) {
                    if(lightOutsideVo.getHour().equals(resultVo.getHour())) {
                        resultVo.setHour(lightOutsideVo.getHour());
                        resultVo.setOutside(lightOutsideVo.getOutside());
                        resultVo.setDimmingRadio(lightOutsideVo.getDimmingRadio());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public LightCountVo getAvgData(AnalyzeDto analyzeDto) {
        return baseMapper.getAvgData(analyzeDto);
    }

    @Override
    public List<InsideAndOutsideVo> insideAndOutsideV2(AnalyzeDto analyzeDto) {
        List<InsideAndOutsideVo> insideAndOutsideVos = tunnelInsideOutsideDayService.list(new LambdaQueryWrapper<TunnelInsideOutsideDay>()
                .eq(TunnelInsideOutsideDay::getTunnelId, analyzeDto.getTunnelId())
                .ge(StringUtils.isNotBlank(analyzeDto.getStartTime()), TunnelInsideOutsideDay::getUploadTime, analyzeDto.getStartTime())
                .le(StringUtils.isNotBlank(analyzeDto.getEndTime()), TunnelInsideOutsideDay::getUploadTime, analyzeDto.getEndTime())
                .orderByDesc(TunnelInsideOutsideDay::getUploadTime)).stream().map(td -> {
            InsideAndOutsideVo insideAndOutsideVo = new InsideAndOutsideVo();
            BeanUtils.copyProperties(td, insideAndOutsideVo);
            return insideAndOutsideVo;
        }).collect(Collectors.toList());

        for (InsideAndOutsideVo insideAndOutsideVo : insideAndOutsideVos) {
            insideAndOutsideVo.setLightRadio(insideAndOutsideVo.getLightUp() + ":" + insideAndOutsideVo.getLightDown());
        }
        return insideAndOutsideVos;
    }
}




