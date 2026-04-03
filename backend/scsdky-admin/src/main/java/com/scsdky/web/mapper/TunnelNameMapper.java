package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Entity generator.domain.TTunnelName
 */
public interface TunnelNameMapper extends BaseMapper<TunnelName> {

    /**
     * 通过用户id获取隧道
     * @param userId
     * @return
     */
    List<TunnelName> getTunnelName(Long userId);
}




