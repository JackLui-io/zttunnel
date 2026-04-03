package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Dashboard 用电/节电概览请求参数
 */
@Data
public class DashboardPowerDto {

    @ApiModelProperty(value = "年份", required = true, example = "2026")
    @NotBlank(message = "年份不能为空")
    @Pattern(regexp = "^(19|20)\\d{2}$", message = "年份格式无效，应为4位数字如2026")
    private String year;
}
