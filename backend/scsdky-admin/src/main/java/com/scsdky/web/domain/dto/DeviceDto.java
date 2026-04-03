package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class DeviceDto {

    @ApiModelProperty(value = "区段")
    private String zone;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备状态")
    private String deviceStatus;

    @ApiModelProperty(value = "搜索关键字")
    private String keyword;

    @ApiModelProperty(value = "隧道id")
    @NotNull(message = "隧道id不能为空")
    private Long tunnelId;

    @ApiModelProperty(value = "类型 1边缘控制器 2电能终端")
    private Integer type = 1;
}
