package com.scsdky.web.domain.response;

import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bomb
 */
public class CommonUtils {
    public static <T> CommonResponse<T> success(T data) {
        CommonResponse<T> res = new CommonResponse<>();
        res.setCode(ResponseCode.SUCCESS.getCode());
        res.setMessage("执行请求成功!");
        res.setData(data);
        return res;
    }
    public static <T> CommonResponse<T> success(T data,String message) {
        CommonResponse<T> res = new CommonResponse<>();
        res.setCode(ResponseCode.SUCCESS.getCode());
        res.setMessage(message);
        res.setData(data);
        return res;
    }

    public static <T> CommonResponse<T> failed(String msg,T data) {
        CommonResponse<T> res = new CommonResponse<>();
        res.setCode(ResponseCode.SYS_ERROR.getCode());
        res.setMessage(msg);
        res.setData(data);
        return res;
    }

    public static <T> CommonResponse<T> failed(Integer code, String msg) {
        CommonResponse<T> res = new CommonResponse<>();
        res.setCode(code);
        res.setMessage(msg);
        return res;
    }

    public static <T, S> CommonResponse<PageDTO<S>> successOfPage(Page<T> page, Function<T, S> convert) {
        PageDTO<S> dto = new PageDTO<>();
        if (page == null || CollectionUtils.isEmpty(page.getContent())) {
            dto.setData(Collections.emptyList());
            dto.setTotal(0);
            return success(dto);
        }
        dto.setTotal((int) page.getTotalElements());
        dto.setData(page.getContent().stream().map(convert).collect(Collectors.toList()));
        return success(dto);
    }

    public static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
