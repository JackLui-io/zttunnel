package com.scsdky.web.service;

import com.scsdky.web.domain.vo.TunnelEdgeTerminalColumnMetaVo;

import java.util.List;

public interface TunnelEdgeTerminalMetaService {

    /**
     * 列出 tunnel_edge_computing_terminal 与实体字段对应的 COMMENT；含虚拟字段 preOnConfig。
     */
    List<TunnelEdgeTerminalColumnMetaVo> listEdgeTerminalColumnMeta();
}
