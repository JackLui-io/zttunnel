package com.scsdky.web.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 新增一级节点（公司 / 管理单位）
 */
@Data
public class TunnelCompanyAddDto {

    @NotBlank(message = "公司名称不能为空")
    private String tunnelName;
}
