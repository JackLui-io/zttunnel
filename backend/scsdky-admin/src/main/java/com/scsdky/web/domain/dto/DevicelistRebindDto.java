package com.scsdky.web.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 将隧道下占位边缘设备号（主键）整体迁移为真实设备号，并同步各关联表中的 devicelist_id。
 */
@Data
public class DevicelistRebindDto {

    @NotNull(message = "原设备号不能为空")
    private Long oldDeviceId;

    @NotNull(message = "新设备号不能为空")
    private Long newDeviceId;
}
