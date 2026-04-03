package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class LoopDto {

    @ApiModelProperty(value = "区段")
    private String zone;

    @ApiModelProperty(value = "1 回路总数列表  2故障列表")
    @NotNull(message = "type不能为空")
    private Integer type;

    @ApiModelProperty(value = "回路状态")
    private String loopStatus;

    @ApiModelProperty(value = "搜索关键字")
    private String keyword;

    @ApiModelProperty(value = "隧道id")
    @NotNull(message = "隧道id不能为空")
    private Long tunnelId;
}
