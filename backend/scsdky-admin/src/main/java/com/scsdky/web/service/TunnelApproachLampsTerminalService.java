package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.domain.dto.TunnelApproachLampsTerminalDto;

import java.util.List;

/**
 * 引道灯控制器(TunnelApproachLampsTerminal)表服务接口
 *
 * @author makejava
 * @since 2025-09-29 10:13:58
 */
public interface TunnelApproachLampsTerminalService extends IService<TunnelApproachLampsTerminal> {

    /**
     * 根据隧道id查询所有引道灯控制器数据
     * @param tunnelId 隧道id
     * @return
     */
    List<TunnelApproachLampsTerminalDto> selectAll(Long tunnelId);
}

