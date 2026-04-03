package com.scsdky.web.domain.vo;

import com.scsdky.web.domain.TunnelDevice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author tubo
 * 隧道信息和设备信息
 * @date 2024/01/30
 */
@Data
@ApiModel(value = "TunnelInfoAndDeviceVo", description = "隧道信息和设备信息")
public class TunnelInfoAndDeviceVo {

    @ApiModelProperty("隧道信息")
    private TunnelNameResultVo tunnelNameResultVo;

    @ApiModelProperty("设备信息")
    private Map<String,List<TunnelDevice>> tunnelDevices;


}
