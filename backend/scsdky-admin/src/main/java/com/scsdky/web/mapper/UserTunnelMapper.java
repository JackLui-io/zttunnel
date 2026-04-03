package com.scsdky.web.mapper;

import com.scsdky.web.domain.UserTunnel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Entity generator.domain.UserTunnel
 */
public interface UserTunnelMapper extends BaseMapper<UserTunnel> {

    List<UserTunnel> getUserTunnelByLevel(Long userId);
}




