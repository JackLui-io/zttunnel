package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("应用参数模板到隧道群")
public class TunnelParamTemplateApplyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @ApiModelProperty(value = "目标 L3 隧道群 id（须无 L4 子节点）", required = true)
    private Long targetTunnelGroupId;

    @NotNull
    @ApiModelProperty(value = "模板 id", required = true)
    private Long templateId;
}
