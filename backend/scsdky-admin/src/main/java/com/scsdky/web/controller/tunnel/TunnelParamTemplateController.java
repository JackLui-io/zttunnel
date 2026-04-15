package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelParamTemplate;
import com.scsdky.web.domain.dto.TunnelParamTemplateApplyDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateDirectionUpdateDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateSaveFromGroupDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateUpdateDto;
import com.scsdky.web.domain.vo.TunnelParamTemplateApplyResultVo;
import com.scsdky.web.domain.vo.TunnelParamTemplateDetailVo;
import com.scsdky.web.service.TunnelParamTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 隧道参数模板（新接口，与 /tunnel/copy 并存）。
 */
@RestController
@RequestMapping("/tunnel/param/template")
@Api(value = "隧道参数模板", tags = {"隧道管理-参数模板"})
public class TunnelParamTemplateController extends BaseController {

    @Resource
    private TunnelParamTemplateService tunnelParamTemplateService;

    @ApiOperation("模板列表（不含已逻辑删除）")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasAnyPermi('tunnel:list:view,tunnel:param:view')")
    public AjaxResult<List<TunnelParamTemplate>> list() {
        return AjaxResult.success(tunnelParamTemplateService.listTemplates());
    }

    @ApiOperation("模板详情（含各方向 payload_json）")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyPermi('tunnel:list:view,tunnel:param:view')")
    public AjaxResult<TunnelParamTemplateDetailVo> detail(@PathVariable Long id) {
        return AjaxResult.success(tunnelParamTemplateService.getDetail(id));
    }

    @ApiOperation("从隧道群存为模板")
    @PostMapping("/save-from-group")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Long> saveFromGroup(@RequestBody @Valid TunnelParamTemplateSaveFromGroupDto dto) {
        Long tid = tunnelParamTemplateService.saveFromGroup(dto, getUsername());
        return AjaxResult.success("保存成功", tid);
    }

    @ApiOperation("更新模板头信息")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Void> updateTemplate(@PathVariable Long id, @RequestBody TunnelParamTemplateUpdateDto dto) {
        tunnelParamTemplateService.updateTemplate(id, dto, getUsername());
        return AjaxResult.success();
    }

    @ApiOperation("更新模板某一方向（含 payload_json）")
    @PutMapping("/{id}/direction/{directionId}")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Void> updateDirection(
        @PathVariable Long id,
        @PathVariable Long directionId,
        @RequestBody TunnelParamTemplateDirectionUpdateDto dto) {
        tunnelParamTemplateService.updateDirection(id, directionId, dto, getUsername());
        return AjaxResult.success();
    }

    @ApiOperation("逻辑删除模板")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        tunnelParamTemplateService.deleteLogical(id, getUsername());
        return AjaxResult.success();
    }

    @ApiOperation("应用模板到无 L4 子节点的隧道群")
    @PostMapping("/apply")
    @PreAuthorize("@ss.hasPermi('system:tunnel:update')")
    public AjaxResult<TunnelParamTemplateApplyResultVo> apply(@RequestBody @Valid TunnelParamTemplateApplyDto dto) {
        TunnelParamTemplateApplyResultVo vo = tunnelParamTemplateService.apply(dto);
        return AjaxResult.success("应用成功", vo);
    }
}
