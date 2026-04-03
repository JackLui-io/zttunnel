package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 设备列表工作台：多隧道（叶子）+ 设备类型 + 分页 + 关键字
 */
@Data
public class DeviceWorkspaceQueryDto {

    @ApiModelProperty(value = "叶子隧道 id 列表（前端由树勾选级联得到）", required = true)
    @NotEmpty(message = "请至少选择一个隧道节点")
    private List<Long> tunnelIds;

    @ApiModelProperty(value = "设备类型：边缘控制器/电能终端/灯具终端/洞外雷达/洞外亮度传感器", required = true)
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @ApiModelProperty("关键字（设备号、回路、桩号等，各类型支持字段略有不同）")
    private String keyword;

    @ApiModelProperty("页码，从 1 开始")
    @Min(value = 1, message = "pageNum 最小为 1")
    private Integer pageNum = 1;

    @ApiModelProperty("每页条数")
    @Min(value = 1, message = "pageSize 最小为 1")
    @Max(value = 500, message = "pageSize 最大为 500")
    private Integer pageSize = 10;
}
