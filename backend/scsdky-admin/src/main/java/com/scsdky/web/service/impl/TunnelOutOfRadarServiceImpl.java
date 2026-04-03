package com.scsdky.web.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelEdgeComputeData;
import com.scsdky.web.domain.TunnelOutOfRadar;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.enums.DeviceZoneEnum;
import com.scsdky.web.mapper.TunnelOutOfRadarMapper;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelEdgeComputeDataService;
import com.scsdky.web.service.TunnelOutOfRadarService;
import com.scsdky.web.utils.ConvertBit;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tubo
 */
@Service
public class TunnelOutOfRadarServiceImpl extends ServiceImpl<TunnelOutOfRadarMapper, TunnelOutOfRadar>
    implements TunnelOutOfRadarService{

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    
    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;

    @Override
    public List<TunnelOutOfRadar> getDeviceDwld(DeviceDto deviceDto) {
        //通过隧道id获取边缘控制器的id
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, deviceDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));

        //处理区段，由于最开始数据库存的是汉字，现在改成整数值，由于前端没改，所以需要将汉字转为1,2,3,4，进行查询
        if(StringUtils.isNotBlank(deviceDto.getZone())){
            deviceDto.setZone(String.valueOf(DeviceZoneEnum.getEnumCode(deviceDto.getZone())));
        }
        List<TunnelOutOfRadar> tunnelOutOfRadars = list(new LambdaQueryWrapper<TunnelOutOfRadar>()
                .eq(TunnelOutOfRadar::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                .eq(TunnelOutOfRadar::getType, deviceDto.getType())
                .eq(StringUtils.isNotBlank(deviceDto.getZone()),TunnelOutOfRadar::getZone, deviceDto.getZone())
                .like(StringUtils.isNotBlank(deviceDto.getKeyword()),TunnelOutOfRadar::getLoopNumber, deviceDto.getKeyword()));
        //setDeviceStatus(list);
        //通过边缘控制器去拿到最新上传的一条数据
        TunnelEdgeComputeData tunnelEdgeComputeData = tunnelEdgeComputeDataService.getOne(Wrappers.lambdaQuery(TunnelEdgeComputeData.class)
                .eq(TunnelEdgeComputeData::getDevicelistId, tunnelDevicelistTunnelinfo.getDevicelistId())
                .orderByDesc(TunnelEdgeComputeData::getUploadTime).last("limit 1"));
        if(tunnelEdgeComputeData != null ) {
            Integer edgeComputeStatus0 = tunnelEdgeComputeData.getEdgeComputeStatus0();
            for (TunnelOutOfRadar tunnelOutOfRadar : tunnelOutOfRadars) {
                //洞外亮度义
                if (deviceDto.getType() == 2) {
                    tunnelOutOfRadar.setDeviceStatus(ConvertBit.computerBit(edgeComputeStatus0, 0) != 0 ? "异常": "正常");
                }

                //洞外雷达
                if (deviceDto.getType() == 1) {
                    tunnelOutOfRadar.setDeviceStatus(ConvertBit.computerBit(edgeComputeStatus0, 1) != 0 ? "异常": "正常");
                }
            }
        }
        return tunnelOutOfRadars;
    }


//    private void setDeviceStatus(List<TunnelOutOfRadar> devices) {
//        //查询设备代码表
//        List<TTunnelDeviceBreakdownInfo> tunnelDeviceBreakdownInfoList = tTunnelDeviceBreakdownInfoService.list();
//        devices.forEach(device -> {
//            if(device.getZone() != null ){
//                device.setZone(DeviceZoneEnum.getEnumValue(device.getZone()));
//            }
//            String deviceNum = String.valueOf(device.getDeviceId()).substring(0, 2);
//            String deviceBreakDown = device.getDeviceStatus();
//            for (TTunnelDeviceBreakdownInfo tTunnelDeviceBreakdownInfo : tunnelDeviceBreakdownInfoList) {
//                if(tTunnelDeviceBreakdownInfo.getDeivceNum().equals(deviceNum) && tTunnelDeviceBreakdownInfo.getBreakdownNum().equals(deviceBreakDown)) {
//                    device.setDeviceStatus(tTunnelDeviceBreakdownInfo.getBreakdownInfo());
//                    break;
//                }
//            }
//        });
//    }

    @Override
    public boolean updateDeviceDwld(TunnelOutOfRadar tunnelOutOfRadar) {
        if(tunnelOutOfRadar.getId() != null ){
            updateById(tunnelOutOfRadar);
        }else{
            Long tunnelId = tunnelOutOfRadar.getTunnelId();
            //通过隧道id获取边缘控制器的id
            TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                    .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId)
                    .eq(TunnelDevicelistTunnelinfo::getType, 1));
            if(tunnelDevicelistTunnelinfo == null  ) {
                throw new BaseException("边缘控制器不存在！");
            }
            tunnelOutOfRadar.setDevicelistId(tunnelDevicelistTunnelinfo.getDevicelistId());
            save(tunnelOutOfRadar);
        }
        return false;
    }
}




