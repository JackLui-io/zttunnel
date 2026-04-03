package com.scsdky.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 * @author tubo
 */
public interface TunnelPowerEdgeComputingService extends IService<TunnelPowerEdgeComputing> {

    /**
     * 查询电表列表：优先按电能终端 deviceListId；否则按四级隧道 tunnelId（该隧道下全部 type=2 终端的电表）；均为空则返回空列表。
     */
    List<TunnelPowerEdgeComputing> powerList(Long deviceListId, Long tunnelId);

    /**
     * 通过id删除电表
     * @param id
     * @return
     */
    boolean dropPower(Long id) throws JsonProcessingException;

    /**
     * 新增和修改
     * @param tunnelPowerEdgeComputing
     * @return
     */
    boolean saveOrUpdatePower(TunnelPowerEdgeComputing tunnelPowerEdgeComputing) throws JsonProcessingException;


    /**
     * 电能终端下发
     * @param devicelistId 电能终端id
     * @return
     */
    boolean issued(Long devicelistId);
}
