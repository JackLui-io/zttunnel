package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelLoopFault;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.dto.LoopDto;
import com.scsdky.web.domain.vo.device.DeviceTypeBaseVo;
import com.scsdky.web.domain.vo.loop.LoopFauitVo;
import com.scsdky.web.enums.LoopStatusEnum;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelLoopFaultService;
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
 * 回路故障
 * @date 2023/07/04
 */

@RestController
@RequestMapping("/loop")
@Api(value = "回路故障", tags = {"DIRECTOR 1.3：回路故障"})
public class TunnelLoopFaultController extends BaseController {

    @Resource
    private TunnelLoopFaultService tunnelLoopFaultService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @ApiOperation("故障列表")
    @PostMapping(value = "/list",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "故障列表", response = LoopFauitVo.class)
    })
    public TableDataInfo getLoopListByPage(@Valid LoopDto loopDto) {
        startPage();
        return getDataTable(tunnelLoopFaultService.getLoopListByPage(loopDto));
    }


    @ApiOperation("灯具列表--excel导出")
    @PostMapping("/list/export")
    public void getLoopListExport(HttpServletResponse response,@Valid DeviceDto deviceDto) {
        List<TunnelLampsTerminal> deviceLamp = tunnelLampsTerminalService.getDeviceLamp(deviceDto);
        ExcelUtil<TunnelLampsTerminal> util = new ExcelUtil<>(TunnelLampsTerminal.class);
        util.exportExcel(response, deviceLamp, "灯具故障列表");
    }

    @ApiOperation("新增或修改回路故障--id 修改必传")
    @PostMapping("/saveOrUpdate")
    public AjaxResult save(@RequestBody TunnelLoopFault tunnelLoopFault) {
        return AjaxResult.success(tunnelLoopFaultService.saveOrUpdate(tunnelLoopFault));
    }

    @ApiOperation("删除回路故障")
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long id) {
        return AjaxResult.success(tunnelLoopFaultService.removeById(id));
    }

    @ApiOperation(value = "统计回路故障数")
    @GetMapping(value = "/countByTunnelId",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = DeviceTypeBaseVo.class)
    })
    public AjaxResult countByTunnelId(@RequestParam Integer tunnelId) {
        return AjaxResult.success(tunnelLoopFaultService.countByTunnelId(tunnelId));
    }

    @ApiOperation(value = "回路状态")
    @GetMapping(value = "/getLoopStatus",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult getLoopStatus() {
        return AjaxResult.success(LoopStatusEnum.getAllData());
    }

}
