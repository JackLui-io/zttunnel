package com.scsdky.web.domain.vo;

import com.scsdky.web.domain.TunnelNotice;
import lombok.Data;

import java.util.List;

/**
 * @author tubo
 * @date 2024/01/08
 */
@Data
public class NoticeDataVo {

    private Integer type;

    private String typeName;

    private Integer typeNum;

    private List<TunnelNotice> tunnelNoticeList;
}
