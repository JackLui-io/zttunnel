package com.scsdky.web.controller.tunnel;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelDeviceBluetooth;
import com.scsdky.web.service.TunnelDeviceBluetoothService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 边缘控制器蓝牙节点信息(TunnelDeviceBluetooth)表控制层
 *
 * @author makejava
 * @since 2025-09-18 15:44:02
 */
@RestController
@RequestMapping("tunnelDeviceBluetooth")
public class TunnelDeviceBluetoothController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private TunnelDeviceBluetoothService tunnelDeviceBluetoothService;

    /**
     * 通过边缘控制器id查询边缘控制器蓝牙节点信息列表
     *
     * @param devicelistId 边缘控制器id
     * @return 单条数据
     */
    @GetMapping("list/{id}")
    public AjaxResult selectList(@PathVariable Long devicelistId) {
        return AjaxResult.success(this.tunnelDeviceBluetoothService.selectList(devicelistId));
    }

}

