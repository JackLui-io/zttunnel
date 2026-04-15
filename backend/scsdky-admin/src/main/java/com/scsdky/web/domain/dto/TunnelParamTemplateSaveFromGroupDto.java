package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("从隧道群存为参数模板")
public class TunnelParamTemplateSaveFromGroupDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @ApiModelProperty(value = "来源 L3 隧道群 id", required = true)
    private Long sourceTunnelGroupId;

    @NotBlank
    @ApiModelProperty(value = "模板名称", required = true)
    private String templateName;

    @ApiModelProperty("模板编码（可选，唯一）")
    private String templateCode;

    @ApiModelProperty("备注")
    private String remark;
}
