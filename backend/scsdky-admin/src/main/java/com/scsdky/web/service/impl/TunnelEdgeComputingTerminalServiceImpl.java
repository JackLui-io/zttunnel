package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.mapper.TunnelEdgeComputingTerminalMapper;
import com.scsdky.web.service.TunnelEdgeComputingTerminalService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
//@DataSource(value = DataSourceType.SLAVE)
public class TunnelEdgeComputingTerminalServiceImpl extends ServiceImpl<TunnelEdgeComputingTerminalMapper, TunnelEdgeComputingTerminal> implements TunnelEdgeComputingTerminalService{

    @Override
    public boolean saveObject(TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal) {
        return save(tunnelEdgeComputingTerminal);
    }

    @Override
    public boolean updateByIdObject(TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal) {
        return updateById(tunnelEdgeComputingTerminal);
    }

    @Override
    public TunnelEdgeComputingTerminal getOneObject(TunnelNameResult tunnelDevice) {
        return getOne(new LambdaQueryWrapper<TunnelEdgeComputingTerminal>()
                .eq(TunnelEdgeComputingTerminal::getId, tunnelDevice.getId()));
    }
}




