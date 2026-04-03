package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.device.DevicePageVo;
import com.scsdky.web.domain.vo.device.DeviceTypeBaseVo;
import com.scsdky.web.enums.DeviceTypeEnum;
import com.scsdky.web.enums.DeviceZoneEnum;
import com.scsdky.web.service.TunnelDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author tubo
 * 隧道--设备列表
 * @date 2023/07/04
 */

@RestController
@RequestMapping("/device")
@Api(value = "设备列表", tags = {"DIRECTOR 1.2：设备列表"})
public class TunnelDeviceController extends BaseController {

    @Resource
    private TunnelDeviceService tTunnelDeviceService;

    @ApiOperation(value = "设备列表",response = DevicePageVo.class)
    @PostMapping(value = "/list",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "设备分页", response = DevicePageVo.class)
    })
    public TableDataInfo getDeviceListByPage(@RequestBody @Valid DeviceDto deviceDto) {
        startPage();
        return getDataTable(tTunnelDeviceService.getDeviceListByPage(deviceDto));
    }


    @ApiOperation("设备列表--excel导出")
    @PostMapping("/list/export")
    public void getDeviceListExport(HttpServletResponse response, DeviceDto deviceDto) {
        List<TunnelDevice> tTunnelDevice = tTunnelDeviceService.getDeviceListByPage(deviceDto);
        ExcelUtil<TunnelDevice> util = new ExcelUtil<>(TunnelDevice.class);
        util.exportExcel(response, tTunnelDevice, "设备列表");
    }

    @ApiOperation(value = "统计设备各个状态数量")
    @GetMapping(value = "/countByTunnelId",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = DeviceTypeBaseVo.class)
    })
    public AjaxResult countByTunnelId(@RequestParam Integer tunnelId) {
        return AjaxResult.success(tTunnelDeviceService.countByTunnelId(tunnelId));
    }

    @ApiOperation("新增或修改设备--id 修改必传")
    @PostMapping("/saveOrUpdate")
    public AjaxResult save(@RequestBody TunnelDevice tTunnelDevice) {
        return AjaxResult.success(tTunnelDeviceService.saveOrUpdate(tTunnelDevice));
    }

    @ApiOperation("删除设备")
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long id) {
        return AjaxResult.success(tTunnelDeviceService.removeById(id));
    }

    @ApiOperation("获取设备状态")
    @GetMapping("/getDeviceStatus")
    public AjaxResult getDeviceStatus() {
        return AjaxResult.success(tTunnelDeviceService.getDeviceStatus());
    }

    @ApiOperation("获取设备类型")
    @GetMapping("/getDeviceType")
    public AjaxResult getDeviceType() {
        return AjaxResult.success(DeviceTypeEnum.getAllData());
    }

    @ApiOperation("获取区段")
    @GetMapping("/getZone")
    public AjaxResult getZone() {
        return AjaxResult.success(DeviceZoneEnum.getAllEnums());
    }
}
