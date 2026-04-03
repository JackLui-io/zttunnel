package com.scsdky.web.service;

import com.scsdky.web.domain.dto.TunnelCopyRequestDto;
import com.scsdky.web.domain.vo.TunnelCopyResultVo;

/**
 * 复制隧道群（level=3）及其 level=4 子节点（隧道树结构）
 */
public interface TunnelCopyService {

    TunnelCopyResultVo copyTunnelGroup(TunnelCopyRequestDto dto);
}
