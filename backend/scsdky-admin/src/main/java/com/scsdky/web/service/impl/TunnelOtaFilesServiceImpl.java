package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.TunnelOtaFiles;
import com.scsdky.web.domain.TunnelSyscmd;
import com.scsdky.web.domain.dto.DeviceListDto;
import com.scsdky.web.domain.dto.OtaBathcDto;
import com.scsdky.web.domain.dto.OtaDto;
import com.scsdky.web.domain.dto.TunnelOtaFilesDto;
import com.scsdky.web.domain.vo.TunnelOtaFilesVo;
import com.scsdky.web.enums.FileDeviceTypeEnum;
import com.scsdky.web.mapper.TunnelDevicelistMapper;
import com.scsdky.web.mapper.TunnelLampsTerminalMapper;
import com.scsdky.web.mapper.TunnelOtaFilesMapper;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelOtaFilesService;
import com.scsdky.web.service.TunnelSyscmdService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ota file文件管理
 * @author tubo
 */
@Service
public class TunnelOtaFilesServiceImpl extends ServiceImpl<TunnelOtaFilesMapper, TunnelOtaFiles> implements TunnelOtaFilesService{

    @Resource
    private TunnelSyscmdService tunnelSyscmdService;
    @Resource
    private TunnelNameResultService tunnelNameResultService;
    @Resource
    private TunnelDevicelistMapper tunnelDevicelistMapper;
    @Resource
    private TunnelLampsTerminalMapper tunnelLampsTerminalMapper;

    @Override
    public String otaOpen(OtaDto otaDto) throws JsonProcessingException, InterruptedException {
        //记录下发指令
        TunnelSyscmd tunnelSyscmd = tunnelSyscmdService.setCmdData(otaDto.getDeviceListId(), "OTA", "", otaDto.getOtaId().toString(), 2);

        TunnelSyscmd tunnelSyscmd1 = tunnelSyscmdService.getById(tunnelSyscmd.getUniqueid());
        if(StringUtils.isNotBlank(tunnelSyscmd1.getRspdata())) {
            return tunnelSyscmd1.getRspdata();
        }else{
            while (true){
                tunnelSyscmd1 = tunnelSyscmdService.getById(tunnelSyscmd.getUniqueid());
                if(StringUtils.isBlank(tunnelSyscmd1.getRspdata())) {
                    Thread.sleep(500L);
                }else{
                    return tunnelSyscmd1.getRspdata();
                }
            }
        }
    }

    @Override
    public List<TunnelOtaFilesVo> getFileList(TunnelOtaFilesDto dto) {
        //筛选查询
        LambdaQueryWrapper<TunnelOtaFiles> queryWrapper = new QueryWrapper<TunnelOtaFiles>()
                .lambda()
                .eq(!ObjectUtils.isEmpty(dto.getVersion()), TunnelOtaFiles::getVersion, dto.getVersion())
                .eq(!ObjectUtils.isEmpty(dto.getDeviceType()), TunnelOtaFiles::getDeviceType, dto.getDeviceType());
        List<TunnelOtaFiles> list = this.list(queryWrapper);
        List<TunnelOtaFilesVo> returnList = list.stream().map(item -> {
            TunnelOtaFilesVo tunnelOtaFilesVo = new TunnelOtaFilesVo();
            BeanUtils.copyProperties(item, tunnelOtaFilesVo);
            return tunnelOtaFilesVo;
        }).collect(Collectors.toList());
//        //获取隧道名称
//        List<TunnelNameResult> resultList = tunnelNameResultService.list();
//        for (TunnelOtaFilesVo vo : returnList) {
//            //递归设置隧道名
//            vo.setTunnelName("");
//            recursiveRetrieval(vo,vo.getTunnelId(),resultList);
//        }
        return returnList;
    }

    @SneakyThrows
    @Override
    public void batchOtaOpen(OtaBathcDto dto) {
        List<Long> deviceIds = dto.getDeviceIds();
        for (Long deviceId : deviceIds) {
            //记录下发指令
            TunnelSyscmd tunnelSyscmd = tunnelSyscmdService.setCmdData(deviceId, "OTA", "",dto.getFileId().toString(), 2);
        }
    }

    @Override
    public List<DeviceListDto> getDeviceList(Long id) {
        TunnelOtaFiles files = this.getById(id);
        if (ObjectUtils.isEmpty(files.getDeviceType()) || ObjectUtils.isEmpty(files.getVersion())){
            return new ArrayList<>();
        }
        FileDeviceTypeEnum deviceTypeEnum = FileDeviceTypeEnum.getByValue(files.getDeviceType());
        ArrayList<DeviceListDto> returnList = new ArrayList<>();
        switch (deviceTypeEnum){
            case EDGE_CONTROLLER:
                List<DeviceListDto> dtoList1 = tunnelDevicelistMapper.selectListByVersion(files.getVersion(), FileDeviceTypeEnum.EDGE_CONTROLLER.getValue());
                returnList.addAll(dtoList1);
                break;
            case ELECTRICITY_TERMINAL:
                List<DeviceListDto> dtoList2 = tunnelDevicelistMapper.selectListByVersion(files.getVersion(), FileDeviceTypeEnum.ELECTRICITY_TERMINAL.getValue());
                returnList.addAll(dtoList2);
                break;
            case REPEATER:
                List<DeviceListDto> dtoList3 = tunnelLampsTerminalMapper.selectListByVersion(files.getVersion());
                returnList.addAll(dtoList3);
                break;
            case LAMP_CONTROLLER:

                break;
            case APPROACH_LAMP_CONTROLLER:

                break;
        }
        //设置隧道名称
        List<TunnelNameResult> resultList = tunnelNameResultService.list();
        Map<Long, List<DeviceListDto>> map = returnList.stream().collect(Collectors.groupingBy(DeviceListDto::getTunnelId));
        for (Map.Entry<Long, List<DeviceListDto>> entry : map.entrySet()) {
            StringBuilder tunnelName = new StringBuilder();
            recursiveRetrieval(tunnelName,entry.getKey(),resultList);
            entry.getValue().stream().forEach(item -> {
                item.setDeviceType(files.getDeviceType());
                item.setTunnelName(tunnelName.toString());
            });
        }
        return returnList;
    }

    /**
     * 递归设置隧道名
     * @param tunnelName 隧道名
     * @param tunnelId 隧道id
     * @param resultList 全部隧道数组
     */
    private void recursiveRetrieval(StringBuilder tunnelName, Long tunnelId, List<TunnelNameResult> resultList) {
        if (ObjectUtils.isEmpty(tunnelId)){
            return;
        }
        List<TunnelNameResult> resultList2 = resultList.stream().filter(item -> item.getId().equals(tunnelId)).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(resultList2)){
            return;
        }
        List<TunnelNameResult> resultList3 = resultList.stream().filter(item -> item.getId().equals(resultList2.get(0).getParentId())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(resultList3)){
            tunnelName.append(resultList2.get(0).getTunnelName());
            return;
        }else {
            tunnelName.append(resultList2.get(0).getTunnelName() + "-");
        }
        recursiveRetrieval(tunnelName,resultList3.get(0).getId(),resultList);
    }

}




