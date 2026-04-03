package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.utils.bean.BeanUtils;
import com.scsdky.web.domain.TunnelDeviceParam;
import com.scsdky.web.domain.vo.TunnelDeviceParamVo;
import com.scsdky.web.mapper.TunnelDeviceParamMapper;
import com.scsdky.web.service.TunnelDeviceParamService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 边缘控制器参数信息(TunnelDeviceParam)表服务实现类
 *
 * @author makejava
 * @since 2025-08-26 09:26:48
 */
@Service("tunnelDeviceParamService")
public class TunnelDeviceParamServiceImpl extends ServiceImpl<TunnelDeviceParamMapper, TunnelDeviceParam> implements TunnelDeviceParamService {

    @Override
    public TunnelDeviceParamVo info(Long devicelistId) {
        //查询数据
        LambdaQueryWrapper<TunnelDeviceParam> queryWrapper = new QueryWrapper<TunnelDeviceParam>()
                .lambda()
                .eq(TunnelDeviceParam::getDevicelistId, devicelistId);
        TunnelDeviceParam param = this.getOne(queryWrapper);
        //没有新增就加一条
        if (ObjectUtils.isEmpty(param)){
            TunnelDeviceParam deviceParam = new TunnelDeviceParam();
            deviceParam.setDevicelistId(devicelistId);
            this.save(deviceParam);
            param = deviceParam;
        }
        //转换返回对象
        TunnelDeviceParamVo tunnelDeviceParamVo = new TunnelDeviceParamVo();
        BeanUtils.copyProperties(param,tunnelDeviceParamVo);
        return tunnelDeviceParamVo;
    }
}

