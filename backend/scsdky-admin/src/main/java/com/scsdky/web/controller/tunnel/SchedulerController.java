package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.service.SchedulerConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 数据汇总定时任务配置接口
 * 供 admin 端开关控制，配置存储在 Redis
 */
@RestController
@RequestMapping("/analyze/scheduler")
@Api(value = "数据汇总", tags = {"数据汇总定时任务配置"})
public class SchedulerController extends BaseController {

    @Resource
    private SchedulerConfigService schedulerConfigService;

    @ApiOperation("获取各任务开关状态（仅 admin）")
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult getStatus() {
        Map<String, Boolean> status = schedulerConfigService.getStatus();
        return AjaxResult.success(status);
    }

    @ApiOperation("更新任务开关状态（仅 admin）")
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult update(@RequestBody Map<String, Object> params) {
        String taskKey = (String) params.get("taskKey");
        Object enabledObj = params.get("enabled");
        if (taskKey == null || taskKey.isEmpty()) {
            return AjaxResult.error("taskKey 不能为空");
        }
        boolean enabled = Boolean.TRUE.equals(enabledObj) || "true".equalsIgnoreCase(String.valueOf(enabledObj));
        schedulerConfigService.setEnabled(taskKey, enabled);
        return AjaxResult.success();
    }
}
