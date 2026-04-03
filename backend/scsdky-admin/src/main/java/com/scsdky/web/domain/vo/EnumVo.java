package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * 枚举返回类
 * @date 2023/09/18
 */
@Data
public class EnumVo {

    @ApiModelProperty("类型编号")
    private Integer typeNum;

    @ApiModelProperty("类型名称")
    private String type;
}
