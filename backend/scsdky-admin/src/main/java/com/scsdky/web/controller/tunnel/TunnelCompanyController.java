package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelCompanyAddDto;
import com.scsdky.web.service.TunnelNameResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 公司（隧道树第一级，level=1）
 */
@RestController
@RequestMapping("/tunnel/company")
@Api(value = "隧道-公司", tags = {"隧道管理-公司"})
public class TunnelCompanyController extends BaseController {

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @ApiOperation("公司列表（level=1）")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('tunnel:list:view')")
    public AjaxResult<List<TunnelNameResult>> list() {
        return AjaxResult.success(tunnelNameResultService.listLevel1Companies());
    }

    @ApiOperation("新增公司（level=1）")
    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Long> add(@RequestBody @Valid TunnelCompanyAddDto dto) {
        return AjaxResult.success(tunnelNameResultService.addLevel1Company(dto.getTunnelName()));
    }
}
