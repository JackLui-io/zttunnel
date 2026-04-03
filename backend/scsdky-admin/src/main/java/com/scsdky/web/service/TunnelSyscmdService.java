package com.scsdky.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.web.domain.TunnelSyscmd;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 * @author tubo
 */
public interface TunnelSyscmdService extends IService<TunnelSyscmd> {

    /**
     *  设置指令下发
     * @param tunnelIdOrDeviceListId 隧道id或者电能表id
     * @param downloadTunnelBaseConfig 指令类型
     * @param ext
     * @param param 参数
     * @param type 1 边缘 2 电能
     * @return
     * @throws JsonProcessingException
     */
    TunnelSyscmd setCmdData(Long tunnelIdOrDeviceListId,String downloadTunnelBaseConfig, String ext, String param,Integer type) throws JsonProcessingException;
}
