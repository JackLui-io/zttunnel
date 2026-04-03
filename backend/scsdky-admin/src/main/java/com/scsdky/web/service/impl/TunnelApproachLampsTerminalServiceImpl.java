package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.dto.TunnelApproachLampsTerminalDto;
import com.scsdky.web.enums.DeviceZoneEnum;
import com.scsdky.web.mapper.TunnelApproachLampsTerminalMapper;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.service.TunnelApproachLampsTerminalService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 引道灯控制器(TunnelApproachLampsTerminal)表服务实现类
 *
 * @author makejava
 * @since 2025-09-29 10:13:58
 */
@Service
public class TunnelApproachLampsTerminalServiceImpl extends ServiceImpl<TunnelApproachLampsTerminalMapper, TunnelApproachLampsTerminal> implements TunnelApproachLampsTerminalService {

    @Override
    public List<TunnelApproachLampsTerminalDto> selectAll(Long tunnelId) {
        //查询列表
        LambdaQueryWrapper<TunnelApproachLampsTerminal> wrapper = new QueryWrapper<TunnelApproachLampsTerminal>()
                .lambda()
                .eq(TunnelApproachLampsTerminal::getTunnelId, tunnelId)
                .orderByDesc(TunnelApproachLampsTerminal::getVersion);
        List<TunnelApproachLampsTerminal> list = this.list(wrapper);
        //转换为DTO
        List<TunnelApproachLampsTerminalDto> dtoList = list.stream().map(item -> {
            TunnelApproachLampsTerminalDto dto = new TunnelApproachLampsTerminalDto();
            BeanUtils.copyProperties(item, dto);
            dto.setZoneName(DeviceZoneEnum.getEnumValue(dto.getZone()));
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }
}

