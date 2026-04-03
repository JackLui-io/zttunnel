package com.scsdky.web.domain.vo;

import lombok.Data;

/**
 * @author tubo
 * @date 2024/01/08
 */
@Data
public class NoticeNumVo {

    private Integer num;

    private String tunnelName;

    private String parentTunnelName;

    private Integer tunnelId;
}
