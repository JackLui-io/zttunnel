package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.web.domain.dto.DeviceWorkspaceQueryDto;
import com.scsdky.web.service.DeviceWorkspaceListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 设备列表工作台（多叶子隧道勾选 + 按类型查询，不修改原 /device/list 等接口）
 */
@RestController
@RequestMapping("/device/workspace")
@Api(value = "设备列表工作台", tags = {"设备列表工作台"})
public class DeviceWorkspaceController extends BaseController {

    @Resource
    private DeviceWorkspaceListService deviceWorkspaceListService;

    @ApiOperation("工作台分页列表（total + rows）")
    @PostMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public TableDataInfo list(@RequestBody @Valid DeviceWorkspaceQueryDto dto) {
        return deviceWorkspaceListService.page(dto);
    }

    @ApiOperation("工作台导出（与 list 相同筛选条件，全量）")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestBody @Valid DeviceWorkspaceQueryDto dto) {
        deviceWorkspaceListService.export(response, dto);
    }
}
