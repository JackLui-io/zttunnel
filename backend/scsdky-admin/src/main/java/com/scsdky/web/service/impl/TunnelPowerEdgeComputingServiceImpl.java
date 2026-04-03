package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.scsdky.web.mapper.TunnelPowerEdgeComputingMapper;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import com.scsdky.web.service.TunnelSyscmdService;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author tubo
 */
@Service
public class TunnelPowerEdgeComputingServiceImpl extends ServiceImpl<TunnelPowerEdgeComputingMapper, TunnelPowerEdgeComputing>
    implements TunnelPowerEdgeComputingService{

    @Resource
    private TunnelSyscmdService tunnelSyscmdService;

    @Override
    public List<TunnelPowerEdgeComputing> powerList(Long deviceListId, Long tunnelId) {
        List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings;
        if (deviceListId != null) {
            tunnelPowerEdgeComputings = this.getBaseMapper().powerList(deviceListId);
        } else if (tunnelId != null) {
            tunnelPowerEdgeComputings = this.getBaseMapper().powerListByTunnelId(tunnelId);
        } else {
            tunnelPowerEdgeComputings = Collections.emptyList();
        }
        if (tunnelPowerEdgeComputings == null) {
            return Collections.emptyList();
        }
        //todo 乡帅隧道临时修改
        tunnelPowerEdgeComputings
                .stream()
                .filter(item -> item.getDevicelistId() != null && item.getDevicelistId().equals(1525080004L) && item.getAddress() != null && item.getAddress().equals(35))
                .forEach(item -> item.setAddress(5));
        return tunnelPowerEdgeComputings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dropPower(Long id) throws JsonProcessingException {
        //获取索引号
        TunnelPowerEdgeComputing tunnelPowerEdgeComputing = getById(id);
        Integer meterIndex = tunnelPowerEdgeComputing.getMeterIndex();
        removeById(id);
        // 删除后，将后续记录的 meterIndex 顺次减一，保持连续性
        List<TunnelPowerEdgeComputing> tunnelPowerEdgeComputings = list(new LambdaQueryWrapper<TunnelPowerEdgeComputing>()
                .gt(TunnelPowerEdgeComputing::getMeterIndex, meterIndex)
                .eq(TunnelPowerEdgeComputing::getDevicelistId, tunnelPowerEdgeComputing.getDevicelistId())
                .orderByAsc(TunnelPowerEdgeComputing::getMeterIndex));

        if(CollectionUtils.isNotEmpty(tunnelPowerEdgeComputings)) {
            int index = meterIndex;
            for (TunnelPowerEdgeComputing entity : tunnelPowerEdgeComputings) {
                entity.setMeterIndex(index++);
            }
            updateBatchById(tunnelPowerEdgeComputings);
        }
        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelPowerEdgeComputing.getDevicelistId(),"DownloadPowerMeter","","",2);
        return true;
    }

    @Override
    public boolean saveOrUpdatePower(TunnelPowerEdgeComputing tunnelPowerEdgeComputing) throws JsonProcessingException {
        Long uniqueId1 = tunnelPowerEdgeComputing.getUniqueId();
        //通过隧道id获取边缘控制器的id
        TunnelPowerEdgeComputing edgeComputing = getOne(new LambdaQueryWrapper<TunnelPowerEdgeComputing>()
                .eq(TunnelPowerEdgeComputing::getDevicelistId, tunnelPowerEdgeComputing.getDevicelistId())
                .eq(TunnelPowerEdgeComputing::getAddress, tunnelPowerEdgeComputing.getAddress()));

        if(uniqueId1 != null ){
            if(edgeComputing != null && !edgeComputing.getUniqueId().equals(tunnelPowerEdgeComputing.getUniqueId())) {
                throw new BaseException("地址号已存在！");
            }
            updateById(tunnelPowerEdgeComputing);
        }else{
            if(edgeComputing != null  ) {
                throw new BaseException("地址号已存在！");
            }
            save(tunnelPowerEdgeComputing);
            // 新增：按当前电能终端已有的最大 meter_index + 1 设置序号
            TunnelPowerEdgeComputing last = getOne(new LambdaQueryWrapper<TunnelPowerEdgeComputing>()
                    .eq(TunnelPowerEdgeComputing::getDevicelistId, tunnelPowerEdgeComputing.getDevicelistId())
                    .orderByDesc(TunnelPowerEdgeComputing::getMeterIndex)
                    .last("limit 1"));
            int nextIndex = last == null ? 0 : (last.getMeterIndex() == null ? 0 : last.getMeterIndex() + 1);
            tunnelPowerEdgeComputing.setMeterIndex(nextIndex);
            updateById(tunnelPowerEdgeComputing);
        }
        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelPowerEdgeComputing.getDevicelistId(),"DownloadPowerMeter","","",2);
        return false;
    }

    @SneakyThrows
    @Override
    public boolean issued(Long devicelistId) {
        //查询终端相关的所有电表
        LambdaQueryWrapper<TunnelPowerEdgeComputing> queryWrapper = new QueryWrapper<TunnelPowerEdgeComputing>()
                .lambda()
                .eq(TunnelPowerEdgeComputing::getDevicelistId, devicelistId);
        List<TunnelPowerEdgeComputing> list = this.list(queryWrapper);
        //记录下发指令
        for (TunnelPowerEdgeComputing tunnelPowerEdgeComputing : list) {
            tunnelSyscmdService.setCmdData(tunnelPowerEdgeComputing.getDevicelistId(),"DownloadPowerMeter","","",2);
        }
        return true;
    }
}




