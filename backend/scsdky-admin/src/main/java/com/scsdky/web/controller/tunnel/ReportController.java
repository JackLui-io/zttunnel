package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.service.TunnelDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;

/**
 * @author tubo
 * @date 2023/12/27
 */
@RestController
@RequestMapping("/report")
@Api(value = "", tags = {"DIRECTOR 1.8：report"})
@Component
public class ReportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Resource
    private TunnelDeviceService tTunnelDeviceService;

    @ApiOperation("report")
    @PostMapping(value = "/count",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult reportCount(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        return AjaxResult.success(tTunnelDeviceService.mileageInfo(analyzeDto));
    }

    @ApiOperation("report")
    @PostMapping(value = "/data",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult reportData(@RequestBody @Valid AnalyzeDto analyzeDto) throws ParseException {
        return AjaxResult.success(tTunnelDeviceService.reportData(analyzeDto));
    }

    @ApiOperation("report-download")
    @PostMapping(value = "/download",produces = {MediaType.APPLICATION_JSON_VALUE})
    public void getWord(HttpServletResponse response, AnalyzeDto analyzeDto)  {
        try {
            tTunnelDeviceService.getWord(response, analyzeDto);
        }catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }
}
