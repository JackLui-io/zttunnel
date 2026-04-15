package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("更新模板方向行（名称、里程、快照 JSON 等）")
public class TunnelParamTemplateDirectionUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("1右线 2左线")
    private Integer direction;

    @ApiModelProperty("默认 L4 隧道名称")
    private String lineDisplayName;

    @ApiModelProperty("默认 L4 里程")
    private BigDecimal lineTunnelMileage;

    @ApiModelProperty("默认 L4 状态 0有效 1失效")
    private Integer lineStatus;

    @ApiModelProperty("整段 payload JSON（与 template_schema_version 一致）")
    private String payloadJson;

    @ApiModelProperty("排序")
    private Integer sortOrder;
}
