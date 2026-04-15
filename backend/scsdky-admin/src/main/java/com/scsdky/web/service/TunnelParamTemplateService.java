package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelParamTemplate;
import com.scsdky.web.domain.dto.TunnelParamTemplateApplyDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateDirectionUpdateDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateSaveFromGroupDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateUpdateDto;
import com.scsdky.web.domain.vo.TunnelParamTemplateApplyResultVo;
import com.scsdky.web.domain.vo.TunnelParamTemplateDetailVo;

import java.util.List;

/**
 * 隧道参数模板：存为模板、应用、维护。
 */
public interface TunnelParamTemplateService {

    List<TunnelParamTemplate> listTemplates();

    TunnelParamTemplateDetailVo getDetail(Long id);

    Long saveFromGroup(TunnelParamTemplateSaveFromGroupDto dto, String username);

    void updateTemplate(Long id, TunnelParamTemplateUpdateDto dto, String username);

    void updateDirection(Long templateId, Long directionId, TunnelParamTemplateDirectionUpdateDto dto, String username);

    void deleteLogical(Long id, String username);

    TunnelParamTemplateApplyResultVo apply(TunnelParamTemplateApplyDto dto);
}
