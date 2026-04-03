package com.scsdky.web.domain.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通用响应类
 * @author tubo
 */
@Data
@ApiModel("基础响应对象")
public class CommonResponse<T> {

    @ApiModelProperty("响应码，200=成功，其他为失败")
    private Integer code;

    @ApiModelProperty("响应消息")
    private String message;

    @ApiModelProperty("响应数据")
    private T data;
}
