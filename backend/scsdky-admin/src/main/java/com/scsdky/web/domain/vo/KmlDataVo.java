package com.scsdky.web.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tubo
 * @date 2024/01/10
 */
@Data
public class KmlDataVo {

    private Long tunnelId;

    private String tunnelName;

    /**
     * 里程数（m）
     */
    private BigDecimal mileage;

    private List<List<BigDecimal>> xyList;
}
