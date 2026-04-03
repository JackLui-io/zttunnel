package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.vo.PlaceholderEdgeTunnelVo;
import com.scsdky.web.service.TunnelPlaceholderBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 绑定列表：复制隧道产生的 9916 开头占位边缘控制器
 */
@RestController
@RequestMapping("/tunnel/bind")
@Api(value = "隧道-绑定列表", tags = {"隧道管理-绑定"})
public class TunnelBindController extends BaseController {

    @Resource
    private TunnelPlaceholderBindService tunnelPlaceholderBindService;

    @ApiOperation("绑定列表：devicelist_id 十进制以 9916 开头时对应的四级隧道（线路名/隧道名/方向）")
    @GetMapping("/placeholder-edge/list")
    @PreAuthorize("@ss.hasPermi('tunnel:list:view')")
    public AjaxResult<List<PlaceholderEdgeTunnelVo>> placeholderEdgeList() {
        return AjaxResult.success(tunnelPlaceholderBindService.listPlaceholderEdgeBindings());
    }

    @ApiOperation("占位 devicelist（设备号十进制以 9916 开头）关联的四级 tunnel_id 列表，供前端与隧道树匹配")
    @GetMapping("/placeholder-tunnel-ids")
    @PreAuthorize("@ss.hasPermi('tunnel:list:view')")
    public AjaxResult<List<Long>> placeholderTunnelIds() {
        return AjaxResult.success(tunnelPlaceholderBindService.listTunnelIdsWith9916PrefixedDevicelist());
    }
}
