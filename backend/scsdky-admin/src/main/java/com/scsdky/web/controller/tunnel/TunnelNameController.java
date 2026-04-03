package com.scsdky.web.controller.tunnel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.DevicelistRebindDto;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.dto.TunnelInfoAndDeviceDto;
import com.scsdky.web.domain.dto.TunnelLampsTerminalDto;
import com.scsdky.web.domain.vo.KmlDataVo;
import com.scsdky.web.domain.vo.TunnelDevicelistVo;
import com.scsdky.web.domain.vo.TunnelInfoAndDeviceVo;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
import com.scsdky.web.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author tubo
 * 隧道管理--左侧
 * @date 2023/07/04
 */

@RestController
@RequestMapping("/tunnel")
@Api(value = "隧道管理", tags = {"DIRECTOR 1.1：隧道管理"})
public class TunnelNameController extends BaseController {

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    private TunnelOutOfRadarService tunnelOutOfRadarService;

    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;

    @ApiOperation("隧道管理--树状显示")
    @GetMapping("/tree/list")
    public AjaxResult<List<TunnelNameResult>> getTunnelName() {
        Long userId = getUserId();
        return AjaxResult.success(tunnelNameResultService.getTunnelName(userId));
    }

    @ApiOperation("隧道管理--所有树状显示")
    @GetMapping("/tree/all/list")
    public AjaxResult<List<TunnelNameResult>> getAllTunnelName() {
        return AjaxResult.success(tunnelNameResultService.getAllTunnelNameTree());
    }

    @ApiOperation("隧道树--新增子节点（前端 /tunnel/add；父为 L1～L3）")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Long> addTunnel(@RequestBody TunnelNameResult body) {
        return AjaxResult.success(tunnelNameResultService.addTunnelTreeNode(body));
    }

    @ApiOperation("隧道树--更新节点（前端 /tunnel/update）")
    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> updateTunnel(@RequestBody TunnelNameResult body) {
        return AjaxResult.success(tunnelNameResultService.updateTunnelTreeNode(body));
    }

    @ApiOperation("隧道管理--查询公路-隧道")
    @GetMapping("/highroad/tunnel")
    public AjaxResult<String> highroadTunnel(@RequestParam Long parentId) {
        return AjaxResult.success(tunnelNameResultService.highroadTunnel(parentId));
    }

    @ApiOperation("上传kml文件")
    @PostMapping("/uoload/kml")
    public AjaxResult<String> uploadKml(@RequestParam("file") MultipartFile file) throws Exception {
        return AjaxResult.success(tunnelNameResultService.uploadKml(file));
    }


    @ApiOperation("查询经纬度--隧道id")
    @GetMapping("/longitudeLatitude")
    public AjaxResult<List<KmlDataVo>> longitudeLatitude(@RequestParam(value = "tunnelId",required = false) Long tunnelId, @RequestParam(value = "isDown",required = false) Integer isDown) throws Exception {
        return AjaxResult.success(tunnelNameResultService.longitudeLatitude(tunnelId,isDown));
    }

    @ApiOperation("分页查询隧道信息")
    @PostMapping("/tunnel/info")
    public TableDataInfo getTunnelInfo(@RequestBody TunnelNameResult tunnelNameResult) {
        startPage();
        Page<TunnelNameResultVo> tunnelInfo = tunnelNameResultService.getTunnelInfo(tunnelNameResult);
        return getDataTable(tunnelInfo);
    }


    @ApiOperation("通过id查看隧道信息和设备信息")
    @GetMapping("/tunnel/device/infoById")
    public AjaxResult<TunnelInfoAndDeviceVo> getTunnelDeviceInfoById(@RequestParam("id") Long id ) {
        return AjaxResult.success(tunnelNameResultService.getTunnelDeviceInfoById(id));
    }

    @ApiOperation("编辑线路和设备信息")
    @PostMapping("/update/tunnel/device/infoById")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> updateTunnelDeviceInfoById(@RequestBody TunnelInfoAndDeviceDto tunnelInfoAndDeviceDto) throws JsonProcessingException {
        return AjaxResult.success(tunnelNameResultService.updateTunnelDeviceInfoById(tunnelInfoAndDeviceDto));
    }


    @ApiOperation("编辑边缘控制器和电能终端")
    @PostMapping("/update/devicelist")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> updateDevicelist(@RequestBody TunnelDevicelist tunnelDevicelist)  {
        return AjaxResult.success(tunnelDevicelistService.updateById(tunnelDevicelist));
    }

    @ApiOperation("边缘/电能终端：设备主键替换（占位号改为真实号，级联更新各表 devicelist_id）")
    @PostMapping("/rebind/devicelist")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> rebindDevicelist(@RequestBody @Valid DevicelistRebindDto dto) {
        return AjaxResult.success(tunnelDevicelistService.rebindDeviceId(dto.getOldDeviceId(), dto.getNewDeviceId()));
    }

    @ApiOperation("编辑灯具终端")
    @PostMapping("/update/device/lamp")
    //@PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> updateDeviceLamp(@RequestBody TunnelLampsTerminalDto dto) throws JsonProcessingException {
        return AjaxResult.success(tunnelLampsTerminalService.updateDeviceLamp(dto));
    }

    @ApiOperation("编辑灯具终端关联节点")
    @PostMapping("/update/device/lamp/node")
    public AjaxResult<Boolean> updateDeviceLampNode(@RequestBody TunnelLampsTerminalDto dto) {
        return AjaxResult.success(tunnelLampsTerminalService.updateDeviceLampNode(dto));
    }

    @ApiOperation("编辑洞外雷达和洞外传感器")
    @PostMapping("/update/device/dwld")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> updateDeviceDwld(@RequestBody TunnelOutOfRadar tunnelOutOfRadar)  {
        return AjaxResult.success(tunnelOutOfRadarService.updateDeviceDwld(tunnelOutOfRadar));
    }


    @ApiOperation("获取边缘控制器和电能终端--通过隧道id")
    @PostMapping("/get/devicelist")
    public AjaxResult<List<TunnelDevicelistVo>> getDevicelist(@RequestBody @Valid DeviceDto deviceDto)  {
        return AjaxResult.success(tunnelDevicelistService.getDevicelist(deviceDto));
    }

    @ApiOperation("获取边缘控制器和电能终端--excel导出")
    @PostMapping("/get/devicelist/export")
    public void getDevicelistExport(HttpServletResponse response, DeviceDto deviceDto) {
        List<TunnelDevicelistVo> tTunnelDevice = tunnelDevicelistService.getDevicelist(deviceDto);
        ExcelUtil<TunnelDevicelistVo> util = new ExcelUtil<>(TunnelDevicelistVo.class);
        util.exportExcel(response, tTunnelDevice, "设备列表");
    }

    @ApiOperation("获取灯具终端")
    @PostMapping("/get/device/lamp")
    public AjaxResult<List<TunnelLampsTerminal>> getDeviceLamp(@RequestBody @Valid DeviceDto deviceDto)  {
        return AjaxResult.success(tunnelLampsTerminalService.getDeviceLamp(deviceDto));
    }

    @ApiOperation("根据灯具终端id获取节点信息")
    @GetMapping("/get/device/lamp/node")
    public AjaxResult<List<TunnelLampsTerminalNode>> getDeviceLampNode(@RequestParam("id") Long id)  {
        return AjaxResult.success(tunnelLampsTerminalService.getDeviceLampNode(id));
    }

    @ApiOperation("删除灯具终端")
    @GetMapping("/drop/device/lamp")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> dropDeviceLamp(@RequestParam Long id) throws JsonProcessingException {
        return AjaxResult.success(tunnelLampsTerminalService.dropDeviceLamp(id));
    }

    @ApiOperation("获取洞外雷达和洞外传感器")
    @PostMapping("/get/device/dwld")
    public AjaxResult<List<TunnelOutOfRadar>> getDeviceDwld(@RequestBody @Valid DeviceDto deviceDto)  {
        return AjaxResult.success(tunnelOutOfRadarService.getDeviceDwld(deviceDto));
    }

    @ApiOperation("获取洞外雷达和洞外传感器--excel导出")
    @PostMapping("/get/device/dwld/export")
    public void getDeviceDwldExport(HttpServletResponse response, DeviceDto deviceDto) {
        List<TunnelOutOfRadar> tTunnelDevice = tunnelOutOfRadarService.getDeviceDwld(deviceDto);
        ExcelUtil<TunnelOutOfRadar> util = new ExcelUtil<>(TunnelOutOfRadar.class);
        util.exportExcel(response, tTunnelDevice, "设备列表");
    }

    @ApiOperation("删除洞外雷达和洞外传感器")
    @GetMapping("/drop/device/dwld")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Boolean> dropDeviceDwld(@RequestParam Long id)  {
        return AjaxResult.success(tunnelOutOfRadarService.removeById(id));
    }

    @ApiOperation("电能终端下发")
    @GetMapping("issued")
    public AjaxResult<Boolean> issued(@RequestParam Long devicelistId)  {
        return AjaxResult.success(tunnelPowerEdgeComputingService.issued(devicelistId));
    }

    @ApiOperation("灯具下发")
    @GetMapping("lamp/issued")
    public AjaxResult<Boolean> lampIssued(@RequestParam Long tunnelId)  {
        return AjaxResult.success(tunnelLampsTerminalService.lampIssued(tunnelId));
    }

}
