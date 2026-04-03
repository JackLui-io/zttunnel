package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelName;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelInfoAndDeviceDto;
import com.scsdky.web.domain.dto.TunnelNameResultExcel;
import com.scsdky.web.domain.vo.KmlDataVo;
import com.scsdky.web.domain.vo.TunnelInfoAndDeviceVo;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
public interface TunnelNameService extends IService<TunnelName> {

    /**
     * 获取树状结构的隧道结构
     * @return
     */
    List<TunnelName> getTunnelName(Long userId);

    /**
     * 获取公路--隧道名称
     * @param parentId
     * @return
     */
    String highroadTunnel(Long parentId);

    /**
     * 上传kml文件并解析经纬度
     * @param file
     * @return
     * @throws Exception
     */
    String uploadKml(MultipartFile file) throws Exception;

    /**
     * 提供隧道的经纬度
     * @param tunnelId
     * @return
     */
    List<KmlDataVo> longitudeLatitude(Long tunnelId,Integer isDown);

    /**
     * 分页查询
     * @param tunnelNameResult
     * @return
     */
    List<TunnelNameResultVo> getTunnelInfo(TunnelNameResult tunnelNameResult);

    /**
     * 通过id获取设备和隧道信息
     * @param id
     * @return
     */
    TunnelInfoAndDeviceVo getTunnelDeviceInfoById(Long id);

    /**
     * 编辑隧道和设备信息
     * @param tunnelInfoAndDeviceDto
     * @return
     */
    boolean updateTunnelDeviceInfoById(TunnelInfoAndDeviceDto tunnelInfoAndDeviceDto);
}
