package com.scsdky.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelLampsTerminalNode;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.dto.TunnelLampsTerminalDto;

import java.util.List;

/**
 *
 */
public interface TunnelLampsTerminalService extends IService<TunnelLampsTerminal> {

    boolean saveObject(TunnelLampsTerminal tunnelLampsTerminal);

    boolean updateObject(TunnelDevice tunnelDevice);

    List<TunnelLampsTerminal> getDeviceLamp(DeviceDto deviceDto);

    /**
     * 多隧道聚合灯具终端（单隧道无边缘时跳过），按 uniqueId 去重。
     */
    List<TunnelLampsTerminal> listDeviceLampByTunnelIds(List<Long> tunnelIds, String keyword);

    /**
     * 删除灯具终端
     * @param id
     * @return
     */
    boolean dropDeviceLamp(Long id) throws JsonProcessingException;

    /**
     * 编辑灯具终端
     * @param dto 灯具终端dto对象
     * @return
     */
    boolean updateDeviceLamp(TunnelLampsTerminalDto dto) throws JsonProcessingException;

    /**
     * 根据灯具终端id获取节点信息
     * @param id 灯具终端id
     * @return 节点信息
     */
    List<TunnelLampsTerminalNode> getDeviceLampNode(Long id);

    /**
     * 编辑灯具终端关联节点
     * @param dto 灯具终端dto对象
     * @return
     */
    Boolean updateDeviceLampNode(TunnelLampsTerminalDto dto);

    /**
     * 灯具批量下发
     * @param tunnelId 隧道id
     * @return
     */
    Boolean lampIssued(Long tunnelId);
}
