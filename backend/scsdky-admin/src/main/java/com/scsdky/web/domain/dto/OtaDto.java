package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author tubo
 * @date 2024/04/15
 */
@Data
public class OtaDto {

    @ApiModelProperty("隧道id")
    @NotNull(message = "隧道id不能为空!")
    private Long tunnelId;

    @ApiModelProperty("ota-ID")
    @NotNull(message = "ota-ID不能为空!")
    private Long otaId;

    @ApiModelProperty("设备id")
    private Long deviceListId;
}
