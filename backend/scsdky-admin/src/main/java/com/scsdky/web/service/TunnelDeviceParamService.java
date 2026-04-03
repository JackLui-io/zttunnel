package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.TunnelDeviceParam;
import com.scsdky.web.domain.vo.TunnelDeviceParamVo;

import java.io.Serializable;

/**
 * 边缘控制器参数信息(TunnelDeviceParam)表服务接口
 *
 * @author makejava
 * @since 2025-08-26 09:26:48
 */
public interface TunnelDeviceParamService extends IService<TunnelDeviceParam> {

    /**
     * 通过边缘控制器id查询单条数据
     * @param devicelistId 边缘控制器id
     * @return 单条数据
     */
    TunnelDeviceParamVo info(Long devicelistId);
}

