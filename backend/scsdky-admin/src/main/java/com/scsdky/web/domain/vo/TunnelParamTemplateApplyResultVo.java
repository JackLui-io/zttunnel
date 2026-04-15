package com.scsdky.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelParamTemplateApplyResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> newLevel4TunnelIds;
}
