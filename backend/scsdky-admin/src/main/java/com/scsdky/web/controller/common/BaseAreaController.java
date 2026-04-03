package com.scsdky.web.controller.common;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.system.service.impl.BaseAreaServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wjs
 * @date 2022/8/2 17:00
 */
@Api("区域获取接口")
@RequestMapping("base/area")
@RestController
public class BaseAreaController extends BaseController {
    @Resource
    BaseAreaServiceImpl baseAreaService;

    @ApiOperation("获取区域树形接口")
    @GetMapping("getAreaTree")
    public AjaxResult getAreaTree(){
        return AjaxResult.success(baseAreaService.selectAreaTree());
    }

    @ApiOperation("获取省份")
    @GetMapping("getProvinces")
    public AjaxResult getProvinces(){
        return AjaxResult.success(baseAreaService.getProvinces());
    }
    @ApiOperation("获取城市")
    @GetMapping("getCities")
    public AjaxResult getCities(Long provinceId){
        if(provinceId == null){
            return AjaxResult.error("请先选择省份");
        }
        return AjaxResult.success(baseAreaService.getCities(provinceId));
    }
}
