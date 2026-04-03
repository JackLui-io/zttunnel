package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.dto.TunnelCopyRequestDto;
import com.scsdky.web.domain.vo.TunnelCopyResultVo;
import com.scsdky.web.service.TunnelCopyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/tunnel/copy")
@Api(value = "隧道复制", tags = {"隧道复制"})
public class TunnelCopyController extends BaseController {

    @Resource
    private TunnelCopyService tunnelCopyService;

    @ApiOperation("复制隧道群（level=3）及下属左右线（level=4）")
    @PostMapping("/tunnelGroup")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<TunnelCopyResultVo> copyTunnelGroup(@RequestBody @Valid TunnelCopyRequestDto dto) {
        TunnelCopyResultVo vo = tunnelCopyService.copyTunnelGroup(dto);
        return AjaxResult.success("复制成功", vo);
    }
}
