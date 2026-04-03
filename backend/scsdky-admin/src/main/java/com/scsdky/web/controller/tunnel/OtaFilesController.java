package com.scsdky.web.controller.tunnel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.dto.*;
import com.scsdky.web.domain.vo.TunnelOtaFilesVo;
import com.scsdky.web.service.TunnelOtaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author tubo
 * ota-file 文件管理
 * @date 2024/04/15
 */

@RestController
@RequestMapping("/ota")
@Api(value = "文件管理", tags = {"DIRECTOR 1.8：ota-file 文件管理"})
public class OtaFilesController extends BaseController {

    @Resource
    private TunnelOtaFilesService tunnelOtaFilesService;

    @ApiOperation("文件列表")
    @GetMapping("/file/list")
    public AjaxResult<List<TunnelOtaFilesVo>> getFileList(TunnelOtaFilesDto dto) {
        return AjaxResult.success(tunnelOtaFilesService.getFileList(dto));
    }

    @ApiOperation("根据文件id查询设备列表")
    @GetMapping("device/list")
    public AjaxResult<List<DeviceListDto>> getDeviceList(@RequestParam Long id) {
        return AjaxResult.success(tunnelOtaFilesService.getDeviceList(id));
    }

    @ApiOperation("启动ota")
    @PostMapping("/open")
    public AjaxResult<String> otaOpen(@RequestBody @Valid OtaDto otaDto) throws JsonProcessingException, InterruptedException {
        return AjaxResult.success(tunnelOtaFilesService.otaOpen(otaDto));
    }

    @ApiOperation("批量启动ota")
    @PostMapping("/batchOpen")
    public AjaxResult batchOtaOpen(@RequestBody @Valid OtaBathcDto dto) {
        tunnelOtaFilesService.batchOtaOpen(dto);
        return AjaxResult.success();
    }

    @ApiOperation("删除ota文件")
    @GetMapping("/del")
    public AjaxResult<Boolean> delOta(Long id ) {
        return AjaxResult.success(tunnelOtaFilesService.removeById(id));
    }


}
