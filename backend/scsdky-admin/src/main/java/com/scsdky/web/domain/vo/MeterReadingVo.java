package com.scsdky.web.domain.vo;

import lombok.Data;

/**
 * @author tubo
 * 电表度数vo
 * @date 2024/10/31
 */
@Data
public class MeterReadingVo {

    /**
     * 回路名称
     */
    private String loopName;

    /**
     * 电表度数
     */
    private String dataValue;
}
