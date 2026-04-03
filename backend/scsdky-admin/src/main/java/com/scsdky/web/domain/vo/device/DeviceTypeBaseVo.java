package com.scsdky.web.domain.vo.device;

import com.scsdky.web.domain.vo.DeviceTypeVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * @date 2023/09/14
 */
@Data
public class DeviceTypeBaseVo {

    @ApiModelProperty(value = "编码",example = "200")
    private Integer code=200;

    @ApiModelProperty(value = "信息",example = "0")
    private String msg;

    @ApiModelProperty(value = "数据")
    private DeviceTypeVo data;
}
