package com.scsdky.web.controller.tunnel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import com.scsdky.web.service.TunnelPowerVendorConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author tubo
 * 电表管理
 * @date 2023/07/04
 */

@RestController
@RequestMapping("/tunnel")
@Api(value = "电表管理", tags = {"DIRECTOR 2.0：电表管理"})
public class TunnelPowerEdgeComputingController extends BaseController {

    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;

    @Resource
    private TunnelPowerVendorConfigService tunnelPowerVendorConfigService;

    @ApiOperation("获取电表：deviceListId 为电能终端号；或传 tunnelId（四级隧道）汇总该隧道下全部电能终端电表")
    @GetMapping("/power/list")
    public AjaxResult powerList(
            @RequestParam(required = false) Long deviceListId,
            @RequestParam(required = false) Long tunnelId) {
        return AjaxResult.success(tunnelPowerEdgeComputingService.powerList(deviceListId, tunnelId));
    }

    @ApiOperation("厂商配置列表")
    @GetMapping("/power/vendor/config")
    public AjaxResult vendorConfigList() {
        return AjaxResult.success(tunnelPowerVendorConfigService.list());
    }

    @ApiOperation("删除电表")
    @GetMapping("/drop/power")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult dropPower(@RequestParam Long id) throws JsonProcessingException {
        return AjaxResult.success(tunnelPowerEdgeComputingService.dropPower(id));
    }


    @ApiOperation("新增和修改")
    @PostMapping("/saveOrUpdate/power")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult saveOrUpdatePower(@RequestBody TunnelPowerEdgeComputing tunnelPowerEdgeComputing) throws JsonProcessingException {
        return AjaxResult.success(tunnelPowerEdgeComputingService.saveOrUpdatePower(tunnelPowerEdgeComputing));
    }

}
