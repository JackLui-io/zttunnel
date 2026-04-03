package com.scsdky.web.controller.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.TunnelDeviceExcel;
import com.scsdky.web.enums.DeviceZoneEnum;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.ConvertBit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author tubo
 * 设备监听，如果多个实体，就监听多个类，创建相同的类
 * @date 2024/01/25
 */
public class DeviceListener extends AnalysisEventListener<TunnelDeviceExcel> {
    /**
     * 单次缓存量为1000
     */
    private final int BATCH_SIZE = 1000;

    private final TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    private final TunnelLampsTerminalService tunnelLampsTerminalService;

    private final TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    private final TunnelNameResultService tunnelNameResultService;

    private final TunnelDevicelistService tunnelDevicelistService;

    private final TunnelOutOfRadarService tunnelOutOfRadarService;

    private String tunnelName = "";

    private String tunnelDirection = "";

    private TunnelNameResult tunnelNameDirection;

    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    /**
     * 临时存储List
     */
    List<TunnelDeviceExcel> cacheData = new ArrayList<>();
    private TunnelDeviceService deviceService;

    public DeviceListener(TunnelDeviceService deviceService, TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService,
                          TunnelLampsTerminalService tunnelLampsTerminalService, TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService,
                          TunnelNameResultService tunnelNameResultService,TunnelDevicelistService tunnelDevicelistService,
                          TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService,
                          TunnelOutOfRadarService tunnelOutOfRadarService) {

        this.deviceService = deviceService;
        this.tunnelEdgeComputingTerminalService = tunnelEdgeComputingTerminalService;
        this.tunnelLampsTerminalService = tunnelLampsTerminalService;
        this.tunnelLampsEdgeComputingService = tunnelLampsEdgeComputingService;
        this.tunnelNameResultService = tunnelNameResultService;
        this.tunnelDevicelistService = tunnelDevicelistService;
        this.tunnelDevicelistTunnelinfoService = tunnelDevicelistTunnelinfoService;
        this.tunnelOutOfRadarService = tunnelOutOfRadarService;
    }

    @Override
    public void invoke(TunnelDeviceExcel data, AnalysisContext analysisContext) {
        cacheData.add(data);
        if (cacheData.size() >= BATCH_SIZE) {
            saveData();
            //每批存储完成后清空list
            cacheData.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (cacheData.size() > 0) {
            saveData();
        }
    }

    /**
     * 加入数据库
     */
    private void saveData() {
        //1.将数据存到我们自己的库中
        //2.将数据存到中间服务器表中
        AtomicReference<Map<String, Long>> deviceIdMap = new AtomicReference<>();
        Map<String, Long> map = new HashMap<>();
        cacheData.forEach(cacheData -> {
            if (StringUtils.isNotBlank(cacheData.getTunnelName())) {
                tunnelName = cacheData.getTunnelName();
            }
            if(StringUtils.isNotBlank(cacheData.getTunnelDirection())) {
                tunnelDirection = cacheData.getTunnelDirection();
                //判断隧道是否存在
                TunnelNameResult tunnelNameResult = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelName));
                tunnelNameDirection = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>()
                        .eq(TunnelNameResult::getParentId, tunnelNameResult.getId())
                        .eq(TunnelNameResult::getTunnelName, tunnelDirection));
            }
            //保存灯具控制器
            if ("边缘控制器".equals(cacheData.getDeviceType()) || "电能终端".equals(cacheData.getDeviceType())) {
                TunnelDevicelist tunnelDevicelist;
                if("边缘控制器".equals(cacheData.getDeviceType())) {
                    tunnelDevicelist = tunnelDevicelistService.getOneObject(cacheData.getDeviceId());
                    if(tunnelDevicelist != null ){
                        throw new BaseException("边缘控制器设备号已存在，请勿重复添加");
                    }
                    //判断这条隧道是否添加了边缘
                    TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfoExist = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                            .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelNameDirection.getId())
                            .eq(TunnelDevicelistTunnelinfo::getType, 1));
                    if(tunnelDevicelistTunnelinfoExist != null ) {
                        throw new BaseException("此隧道边缘控制器设备已添加，请勿重复添加");
                    }

                    tunnelDevicelist = new TunnelDevicelist();
                    tunnelDevicelist.setDeviceId(Long.valueOf(cacheData.getDeviceId()));
                    tunnelDevicelist.setNickName(cacheData.getDeviceType());
                    tunnelDevicelist.setDeviceNum(ConvertBit.deviceNumConvertValue(cacheData.getDeviceNum()));
                    tunnelDevicelist.setDeviceTypeId(1);
                    tunnelDevicelistService.saveObject(tunnelDevicelist);
                    //保存隧道和边缘控制器的关系
                    TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = new TunnelDevicelistTunnelinfo();
                    tunnelDevicelistTunnelinfo.setDevicelistId(Long.valueOf(cacheData.getDeviceId()));
                    tunnelDevicelistTunnelinfo.setTunnelId(tunnelNameDirection.getId());
                    tunnelDevicelistTunnelinfo.setType(1);
                    tunnelDevicelistTunnelinfoService.saveObject(tunnelDevicelistTunnelinfo);

                }
                //如果电能终端存在，就不存
                if("电能终端".equals(cacheData.getDeviceType())){
                    tunnelDevicelist = tunnelDevicelistService.getOneObject(cacheData.getDeviceId());
                    if(tunnelDevicelist == null ){
                        tunnelDevicelist = new TunnelDevicelist();
                        tunnelDevicelist.setDeviceId(Long.valueOf(cacheData.getDeviceId()));
                        tunnelDevicelist.setNickName(cacheData.getDeviceType());
                        tunnelDevicelist.setDeviceNum(ConvertBit.deviceNumConvertValue(cacheData.getDeviceNum()));
                        tunnelDevicelist.setDeviceTypeId(2);
                        tunnelDevicelistService.saveObject(tunnelDevicelist);
                    }
                    //保存隧道和边缘控制器的关系
                    TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = new TunnelDevicelistTunnelinfo();
                    tunnelDevicelistTunnelinfo.setDevicelistId(Long.valueOf(cacheData.getDeviceId()));
                    tunnelDevicelistTunnelinfo.setTunnelId(tunnelNameDirection.getId());
                    tunnelDevicelistTunnelinfo.setType(2);
                    tunnelDevicelistTunnelinfoService.saveObject(tunnelDevicelistTunnelinfo);
                }

                if("边缘控制器".equals(cacheData.getDeviceType())) {
                    map.put(tunnelName + tunnelDirection, Long.valueOf(cacheData.getDeviceId()));
                }
            }
        });
        //说明没有边缘控制器，必须强制添加
        if(map.size() == 0 ){
            throw new BaseException("边缘控制器不能为空!");
        }

        deviceIdMap.set(map);

        //灯具序号
        int postion = 0;
        //灯具控制器个数，每个隧道不超过160个
        int num = 0;
        //灯具控制器数量判断
        for (TunnelDeviceExcel device : cacheData) {
            if(StringUtils.isNotBlank(device.getTunnelName())) {
                num = 0;
            }
            if("灯具控制器".equals(device.getDeviceType())) {
                num++;
            }
            if(num > 160 ) {
                throw new BaseException("灯具终端不能超过160个!");
            }
        }
        //除开边缘和电能终端其他的设备
        for (TunnelDeviceExcel device : cacheData) {
            if (StringUtils.isNotBlank(device.getTunnelName())) {
                tunnelName = device.getTunnelName();
            }
            if(StringUtils.isNotBlank(device.getTunnelDirection())) {
                tunnelDirection = device.getTunnelDirection();
            }
            if ("灯具控制器".equals(device.getDeviceType())) {
                TunnelLampsTerminal tunnelLampsTerminal = new TunnelLampsTerminal();
                tunnelLampsTerminal.setDeviceId(Long.valueOf(device.getDeviceId()));
                tunnelLampsTerminal.setDeviceProperty(device.getDeviceProperty());
                //tunnelLampsTerminal.setPosition(device.getPosition());
                tunnelLampsTerminal.setPosition(postion);

                //这里本来应该是int类型，改为String了，数据库由于不能改为String，所以只能这样了
                tunnelLampsTerminal.setDeviceNum(ConvertBit.deviceNumConvertValue(device.getDeviceNum()));
                tunnelLampsTerminal.setDeviceStatus(device.getDeviceStatus());
                tunnelLampsTerminal.setLdDeviceId(device.getLdDviceId());
                tunnelLampsTerminal.setLdWhetherInstall(device.getLdWhetherInstall());
                tunnelLampsTerminal.setLoopNumber(device.getLoopNumber());
                tunnelLampsTerminal.setZone(device.getZone());
                tunnelLampsTerminal.setLdStatus(device.getLdStatus());
                //保存提供给唐总的设备表
                tunnelLampsTerminalService.saveObject(tunnelLampsTerminal);
                //保存设备和边缘控制器之前的关系表
                TunnelLampsEdgeComputing tunnelLampsEdgeComputing = new TunnelLampsEdgeComputing();
                tunnelLampsEdgeComputing.setUniqueId(tunnelLampsTerminal.getUniqueId());
                tunnelLampsEdgeComputing.setDevicelistId(deviceIdMap.get().get(tunnelName + tunnelDirection));
                tunnelLampsEdgeComputingService.saveObject(tunnelLampsEdgeComputing);
                postion++;
            }
            if ("洞外雷达".equals(device.getDeviceType()) || "洞外亮度传感器".equals(device.getDeviceType())) {
                TunnelOutOfRadar tunnelOutOfRadar = new TunnelOutOfRadar();
                tunnelOutOfRadar.setDeviceId(Long.valueOf(device.getDeviceId()));
                tunnelOutOfRadar.setDevicelistId(deviceIdMap.get().get(tunnelName + tunnelDirection));
                tunnelOutOfRadar.setDeviceStatus(device.getDeviceStatus());
                tunnelOutOfRadar.setDeviceNum(ConvertBit.deviceNumConvertValue(device.getDeviceNum()));
                tunnelOutOfRadar.setLoopNumber(device.getLoopNumber());
                tunnelOutOfRadar.setZone(device.getZone());
                tunnelOutOfRadar.setType("洞外雷达".equals(device.getDeviceType()) ? 1 : 2);
                //保存提供给唐总的设备表
                tunnelOutOfRadarService.save(tunnelOutOfRadar);
            }
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            int row = excelDataConvertException.getRowIndex() + 1;
            int column = excelDataConvertException.getColumnIndex() + 1;
            throw new RuntimeException("第" + row + "行，第" + column + "列解析异常，请正确填写");
        }
    }

   /* private TunnelDevice buildDevice(TunnelDeviceExcel tunnelDeviceExcel) {
        if (StringUtils.isNotBlank(tunnelDeviceExcel.getTunnelName())) {
            tunnelName = tunnelDeviceExcel.getTunnelName();

        }
        if(StringUtils.isNotBlank(tunnelDeviceExcel.getTunnelDirection())) {
            tunnelDirection = tunnelDeviceExcel.getTunnelDirection();
            //判断隧道是否存在
            TunnelNameResult tunnelNameResult = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelName));
            tunnelNameDirection = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>()
                    .eq(TunnelNameResult::getParentId, tunnelNameResult.getId())
                    .eq(TunnelNameResult::getTunnelName, tunnelDirection));
        }
        return TunnelDevice.builder()
                .tunnelName(tunnelDirection)
                .tunnelId(tunnelNameDirection.getId())
                .loopNumber(tunnelDeviceExcel.getLoopNumber())
                .deviceId(tunnelDeviceExcel.getDeviceId())
                .deviceNum(tunnelDeviceExcel.getDeviceNum())
                .deviceName(tunnelDeviceExcel.getDeviceName())
                .deviceType(tunnelDeviceExcel.getDeviceType())
                .deviceStatus(tunnelDeviceExcel.getDeviceStatus())
                .deviceProperty(tunnelDeviceExcel.getDeviceProperty())
                .ldDeviceId(tunnelDeviceExcel.getDeviceId())
                .ldStatus(tunnelDeviceExcel.getLdStatus())
                .ldWhetherInstall(StringUtils.isNotBlank(tunnelDeviceExcel.getLdWhetherInstall()) ? Integer.parseInt(tunnelDeviceExcel.getLdWhetherInstall()) : 0).build();
    }*/
}
