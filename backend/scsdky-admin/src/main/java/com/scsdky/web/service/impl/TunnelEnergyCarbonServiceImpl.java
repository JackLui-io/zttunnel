package com.scsdky.web.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.EnergyCarbonVo;
import com.scsdky.web.mapper.TunnelEleMeterMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.mapper.TunnelEnergyCarbonMapper;
import com.scsdky.web.utils.DateUtils;
import com.scsdky.web.utils.ExportExcelUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelEnergyCarbonServiceImpl extends ServiceImpl<TunnelEnergyCarbonMapper, TunnelEnergyCarbon> implements TunnelEnergyCarbonService{

    @Resource
    TunnelEleMeterMapper tunnelEleMeterMapper;

    @Resource
    TunnelDeviceService tunnelDeviceService;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Override
    public List<EnergyCarbonVo> carbon(AnalyzeDto analyzeDto) {
        List<EnergyCarbonVo> energyCarbonVos = baseMapper.carbon(analyzeDto);
//        energyCarbonVos.forEach(energyCarbonVo -> {
//            //构建电表读数，模拟的，后期传递进来会修改
//            Long tunnelId = analyzeDto.getTunnelId();
//            List<Integer> data = new ArrayList<>();
//            for (int i = 0; i < tunnelId; i++) {
//                data.add(1);
//            }
//            energyCarbonVo.setMeterReadings(data);
//            //理论节电率
//            double v2 = (144 * 1.0 - energyCarbonVo.getDailyPowerConsumption()) / 144;
//            energyCarbonVo.setTheoreticalPowerSavingRate(new BigDecimal(v2).multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.UP));
//            //理论节电量
//            BigDecimal multiply = new BigDecimal(energyCarbonVo.getDailyPowerConsumption()).multiply(energyCarbonVo.getTheoreticalPowerSavingRate());
//            energyCarbonVo.setTheoreticalPowerSavings(multiply);
//        });
        return energyCarbonVos;
    }

    @Override
    public void exportCarbon(HttpServletResponse response, AnalyzeDto analyzeDto) {
        // 获取隧道下电表推送的数据
        List<TunnelEleMeter> tunnelEleMeters = tunnelEleMeterMapper.selectEleMeterByTunnelId(analyzeDto);
        LinkedHashMap<String,  Map<String, TunnelEleMeter>> tunnelEleMeterMap = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        for (TunnelEleMeter tunnelEleMeter:tunnelEleMeters) {
            String key = DateUtil.format(tunnelEleMeter.getUploadTime(),"yyyy-MM-dd HH:mm");
            if(tunnelEleMeterMap.containsKey(key)){
                tunnelEleMeterMap.get(key).put(tunnelEleMeter.getDeviceNum(),tunnelEleMeter);
            } else {
                Map<String, TunnelEleMeter> oneEleMeterMap = new HashMap<>();
                oneEleMeterMap.put(tunnelEleMeter.getDeviceNum(),tunnelEleMeter);
                tunnelEleMeterMap.put(key, oneEleMeterMap);
                keys.add(key);
            }
        }

        // 获取设备对象
        DeviceDto deviceDto = new DeviceDto();
        deviceDto.setTunnelId(analyzeDto.getTunnelId());
        deviceDto.setDeviceType("电表");
        List<TunnelDevice> tunnelDevices = tunnelDeviceService.getDeviceListByPage(deviceDto);

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
            ExportExcelUtils.carbonExportTemp(response, title, dateRange, keys,tunnelEleMeterMap,tunnelDevices);
        } catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("能碳数据导出失败！");
        }
    }

}




