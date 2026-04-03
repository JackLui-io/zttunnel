package com.scsdky.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.web.domain.TunnelOtaFiles;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.DeviceListDto;
import com.scsdky.web.domain.dto.OtaBathcDto;
import com.scsdky.web.domain.dto.OtaDto;
import com.scsdky.web.domain.dto.TunnelOtaFilesDto;
import com.scsdky.web.domain.vo.TunnelOtaFilesVo;

import java.util.List;

/**
 * @author tubo
 */
public interface TunnelOtaFilesService extends IService<TunnelOtaFiles> {

    /**
     * ota文件启动
     * @param otaDto
     * @return
     * @throws JsonProcessingException
     */
    String otaOpen(OtaDto otaDto) throws JsonProcessingException, InterruptedException;

    /**
     * 文件列表
     * @param dto 筛选
     * @return
     */
    List<TunnelOtaFilesVo> getFileList(TunnelOtaFilesDto dto);

    /**
     * 批量启动ota
     * @param dto 请求参数
     * @return
     */
    void batchOtaOpen(OtaBathcDto dto);

    /**
     * 根据文件id查询设备列表
     * @param id 文件id
     * @return 设备列表
     */
    List<DeviceListDto> getDeviceList(Long id);
}
