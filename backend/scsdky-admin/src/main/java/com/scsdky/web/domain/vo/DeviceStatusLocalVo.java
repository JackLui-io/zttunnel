package com.scsdky.web.domain.vo;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * 洞内洞外照度
 * @date 2023/09/18
 */
@Data
public class DeviceStatusLocalVo {

    @ApiModelProperty(value = "设备类别")
    private String deviceType;

    @ApiModelProperty(value = "设备号")
    private Long deviceId;

    @ApiModelProperty(value = "安装位置")
    private String installationPosition;

    @ApiModelProperty(value = "设备状态")
    private String deviceStatus;


}
