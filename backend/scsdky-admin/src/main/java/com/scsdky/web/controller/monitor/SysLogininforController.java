package com.scsdky.web.controller.monitor;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scsdky.common.annotation.Log;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.enums.BusinessType;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.system.domain.SysLogininfor;
import com.scsdky.system.service.ISysLogininforService;

/**
 * 系统访问记录
 */
@RestController
@RequestMapping("/monitor/logininfor")
@Api(value = "访问记录", tags = {"DIRECTOR 1.6：访问记录"})
public class SysLogininforController extends BaseController
{
    @Autowired
    private ISysLogininforService logininforService;

    //@PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
    @GetMapping("/list")
    @ApiOperation("访问记录列表")
    public TableDataInfo list(SysLogininfor logininfor)
    {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor)
    {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        logininforService.cleanLogininfor();
        return AjaxResult.success();
    }
}
