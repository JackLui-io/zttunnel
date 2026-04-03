package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.TunnelPowerSupplyCircuit;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitDto;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitQueryDto;
import com.scsdky.web.mapper.TunnelPowerSupplyCircuitMapper;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelPowerSupplyCircuitService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 直流照明-供电回路表(com.scsdky.web.test.TunnelPowerSupplyCircuit)表服务实现类
 *
 * @author makejava
 * @since 2025-08-25 13:54:06
 */
@Service("tunnelPowerSupplyCircuitService")
public class TunnelPowerSupplyCircuitServiceImpl extends ServiceImpl<TunnelPowerSupplyCircuitMapper, TunnelPowerSupplyCircuit> implements TunnelPowerSupplyCircuitService {

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Override
    public List<TunnelPowerSupplyCircuit> selectAll(TunnelPowerSupplyCircuitQueryDto dto) {
        LambdaQueryWrapper<TunnelPowerSupplyCircuit> queryWrapper = new QueryWrapper<TunnelPowerSupplyCircuit>()
                .lambda()
                .eq(TunnelPowerSupplyCircuit::getTunnelId, dto.getTunnelId());
        return this.list(queryWrapper);
    }

    @Override
    public Boolean insert(TunnelPowerSupplyCircuitDto dto) {
        //判断隧道是否存在
        TunnelNameResult result = tunnelNameResultService.getById(dto.getTunnelId());
        if (ObjectUtils.isEmpty(result)){
            throw new BaseException("绑定的隧道不存在！");
        }
        TunnelPowerSupplyCircuit entity = new TunnelPowerSupplyCircuit();
        BeanUtils.copyProperties(dto,entity);
        return this.save(entity);
    }

    @Override
    public Boolean updateData(TunnelPowerSupplyCircuitDto dto) {
        //判断隧道是否存在
        TunnelNameResult result = tunnelNameResultService.getById(dto.getTunnelId());
        if (ObjectUtils.isEmpty(result)){
            throw new BaseException("绑定的隧道不存在！");
        }
        TunnelPowerSupplyCircuit entity = new TunnelPowerSupplyCircuit();
        BeanUtils.copyProperties(dto,entity);
        return this.updateById(entity);
    }
}

