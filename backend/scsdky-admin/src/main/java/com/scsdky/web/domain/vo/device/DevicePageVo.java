package com.scsdky.web.domain.vo.device;

import com.scsdky.web.domain.TunnelDevice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author tubo
 * @date 2023/09/14
 */
@Data
@ApiModel("设备分页")
public class DevicePageVo {
    private static final long serialVersionUID = -1965666644468335323L;

    @ApiModelProperty(value = "编码",example = "200")
    private Integer code=200;

    @ApiModelProperty(value = "信息",example = "0")
    private String msg;

    @ApiModelProperty(value = "数据")
    private List<TunnelDevice> rows;

    @ApiModelProperty(value = "总页数")
    private long total;
}
