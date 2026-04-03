package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelDeviceExcel;

/**
 *
 */
public interface TunnelEdgeComputingTerminalService extends IService<TunnelEdgeComputingTerminal> {

    boolean saveObject(TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal);

    boolean updateByIdObject(TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal);

    TunnelEdgeComputingTerminal getOneObject(TunnelNameResult tunnelDevice);
}
