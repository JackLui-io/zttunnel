package com.scsdky.web.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 复制隧道群（level=3）及下属左右线、设备等到同一路段下
 */
@Data
public class TunnelCopyRequestDto {

    @NotNull(message = "源隧道群ID不能为空")
    private Long sourceTunnelGroupId;

    @NotBlank(message = "新隧道名称不能为空")
    private String newTunnelName;
}
