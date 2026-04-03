package com.scsdky.web.domain.dto;

import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
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
public class TunnelInfoAndDeviceDto {

    @ApiModelProperty("隧道信息")
    private TunnelNameResultVo tunnelNameResultVo;

    @ApiModelProperty("设备信息")
    private List<TunnelDevice> tunnelDevices;


}
