package com.scsdky.web.domain.vo;

import com.scsdky.web.domain.TunnelParamTemplate;
import com.scsdky.web.domain.TunnelParamTemplateDirection;
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
public class TunnelParamTemplateDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private TunnelParamTemplate template;
    private List<TunnelParamTemplateDirection> directions;
}
