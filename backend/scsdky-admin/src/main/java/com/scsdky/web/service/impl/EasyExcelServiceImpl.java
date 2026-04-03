package com.scsdky.web.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.scsdky.web.controller.listener.DeviceListener;
import com.scsdky.web.controller.listener.TunnelNameListener;
import com.scsdky.web.domain.dto.TunnelDeviceExcel;
import com.scsdky.web.domain.dto.TunnelNameResultExcel;
import com.scsdky.web.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * @author tubo
 * @date 2024/01/25
 */
@Service
public class EasyExcelServiceImpl implements IEasyExcelService {

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelDeviceService tunnelDeviceService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelOutOfRadarService tunnelOutOfRadarService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void excelInput(MultipartFile file) throws Exception{
        InputStream inputStream = file.getInputStream();
        ExcelReader excelReader = EasyExcel.read(inputStream).build();
        ReadSheet sheet = EasyExcel.readSheet("新建隧道").head(TunnelNameResultExcel.class)
                .registerReadListener(new TunnelNameListener(tunnelNameResultService,tunnelEdgeComputingTerminalService))
                .build();
        ReadSheet sheet1 = EasyExcel.readSheet("新建设备").head(TunnelDeviceExcel.class)
                .registerReadListener(new DeviceListener(tunnelDeviceService,tunnelEdgeComputingTerminalService,
                        tunnelLampsTerminalService,tunnelLampsEdgeComputingService,
                        tunnelNameResultService,tunnelDevicelistService,tunnelDevicelistTunnelinfoService,tunnelOutOfRadarService))
                .build();
        excelReader.read(sheet);
        excelReader.read(sheet1);
        excelReader.finish();
    }
}
