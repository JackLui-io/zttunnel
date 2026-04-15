package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("更新参数模板头信息")
public class TunnelParamTemplateUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("模板编码（可置空；非空时需唯一）")
    private String templateCode;

    @ApiModelProperty("0草稿 1启用 2停用")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;
}
