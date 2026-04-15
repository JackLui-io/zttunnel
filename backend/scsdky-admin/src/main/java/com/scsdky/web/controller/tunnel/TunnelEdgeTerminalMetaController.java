package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.vo.TunnelEdgeTerminalColumnMetaVo;
import com.scsdky.web.service.TunnelEdgeTerminalMetaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 边缘计算终端表字段元数据（列 COMMENT），供模板/参数页与库表对齐展示。
 */
@RestController
@RequestMapping("/tunnel/edge-terminal")
@Api(value = "边缘计算终端元数据", tags = {"隧道管理-参数"})
public class TunnelEdgeTerminalMetaController extends BaseController {

    @Resource
    private TunnelEdgeTerminalMetaService tunnelEdgeTerminalMetaService;

    @ApiOperation("tunnel_edge_computing_terminal 字段 COMMENT列表（含虚拟 preOnConfig）")
    @GetMapping("/column-meta")
    @PreAuthorize("@ss.hasAnyPermi('tunnel:list:view,tunnel:param:view')")
    public AjaxResult<List<TunnelEdgeTerminalColumnMetaVo>> columnMeta() {
        return AjaxResult.success(tunnelEdgeTerminalMetaService.listEdgeTerminalColumnMeta());
    }
}
