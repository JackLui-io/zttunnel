package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.DeviceTypeVo;
import com.scsdky.web.domain.vo.ReportVo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface TunnelDeviceService extends IService<TunnelDevice> {

    /**
     * 获取设备列表
     * @param deviceDto
     * @return
     */
    List<TunnelDevice> getDeviceListByPage(DeviceDto deviceDto);

    /**
     * 统计设备各个状态的数量
     * @param tunnelId
     * @return
     */
    DeviceTypeVo countByTunnelId(Integer tunnelId);

    /**
     * Dashboard 设备状态分布：按隧道ID列表汇总在线/离线/故障数量
     * @param tunnelIds 隧道ID列表（level-4），为空时返回全0
     * @return
     */
    DeviceTypeVo countByTunnelIds(java.util.List<Long> tunnelIds);

    /**
     * 获取设备状态
     * @return
     */
    List<String> getDeviceStatus();

    /**
     * 获取设备类型
     * @return
     */
    List<String> getDeviceType();

    /**
     *
     * @param analyzeDto
     */
    Map<String, Object> mileageInfo(AnalyzeDto analyzeDto) throws ParseException;

    /**
     * 生成word报告
     * @param response
     * @param analyzeDto
     * @throws IOException
     */
    void getWord(HttpServletResponse response,AnalyzeDto analyzeDto) throws IOException, ParseException;

    /**
     * word数据
     * @param analyzeDto
     * @return
     */
    Map<String, Object> reportData(AnalyzeDto analyzeDto) throws ParseException;
}
