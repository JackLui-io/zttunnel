package com.scsdky.web.controller.tunnel;


import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.service.TunnelDeviceParamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 边缘控制器参数信息(TunnelDeviceParam)表控制层
 *
 * @author makejava
 * @since 2025-08-26 09:26:41
 */
@RestController
@RequestMapping("tunnelDeviceParam")
public class TunnelDeviceParamController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private TunnelDeviceParamService tunnelDeviceParamService;

    /**
     * 通过边缘控制器id查询边缘控制器参数信息单条数据
     * @param devicelistId 边缘控制器id
     * @return 单条数据
     */
    @GetMapping("info/{id}")
    public AjaxResult info(@PathVariable Long devicelistId) {
        return AjaxResult.success(this.tunnelDeviceParamService.info(devicelistId));
    }

}

