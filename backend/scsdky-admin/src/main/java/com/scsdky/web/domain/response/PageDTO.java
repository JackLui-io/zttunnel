package com.scsdky.web.domain.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/***
 * 分页查询响应
 *
 * @author stevenkang-X550J on 2021/1/9
 */
@Data
@ApiModel("分页查询响应")
public class PageDTO<T> {

    @ApiModelProperty("响应数据")
    private List<T> data;

    @ApiModelProperty("总条数")
    private Integer total;

    public static <T> PageDTO<T> empty() {
         PageDTO<T> page = new PageDTO<>();
         page.setTotal(0);
         page.setData(Collections.emptyList());
         return page;
    }
}
