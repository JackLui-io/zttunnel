package com.scsdky.web.domain.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/***
 * 响应码
 *
 * @author stevenkang-X550J on 2021/1/16
 */
@Getter
@AllArgsConstructor
@ApiModel("响应码")
public enum ResponseCode {

    SUCCESS(0, "执行请求成功!"),

    INVALID_PARAM(101, "参数有误"),

    REQUEST_FAIL(102, "请求失败"),

    BALANCE_IS_NOT_ENOUGH(108,"余额不足"),

    UNAUTHORIZED(401,"登录已过期"),

    SYS_ERROR(999, "执行请求异常!");

    private final Integer code;

    private final String message;
}
