package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelPowerSupplyCircuit;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitDto;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitQueryDto;

import java.util.List;

/**
 * 直流照明-供电回路表(com.scsdky.web.test.TunnelPowerSupplyCircuit)表服务接口
 *
 * @author makejava
 * @since 2025-08-25 13:54:06
 */
public interface TunnelPowerSupplyCircuitService extends IService<TunnelPowerSupplyCircuit> {
    /**
     * 查询所有数据
     *
     * @param dto 查询实体
     * @return 所有数据
     */
    List<TunnelPowerSupplyCircuit> selectAll(TunnelPowerSupplyCircuitQueryDto dto);

    /**
     * 新增数据
     *
     * @param dto 实体对象
     * @return 新增结果
     */
    Boolean insert(TunnelPowerSupplyCircuitDto dto);

    /**
     * 修改数据
     *
     * @param dto 实体对象
     * @return 修改结果
     */
    Boolean updateData(TunnelPowerSupplyCircuitDto dto);
}

