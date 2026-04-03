package com.scsdky.web.domain.vo.loop;

import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelLoopFault;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author tubo
 * @date 2023/09/14
 */
@Data
public class LoopFauitVo {

    @ApiModelProperty(value = "编码",example = "200")
    private Integer code=200;

    @ApiModelProperty(value = "信息",example = "0")
    private String msg;

    @ApiModelProperty(value = "数据")
    private List<TunnelLoopFault> rows;

    @ApiModelProperty(value = "总页数")
    private long total;
}
