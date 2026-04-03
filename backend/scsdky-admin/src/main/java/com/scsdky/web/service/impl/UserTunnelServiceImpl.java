package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.UserTunnel;
import com.scsdky.web.service.UserTunnelService;
import com.scsdky.web.mapper.UserTunnelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class UserTunnelServiceImpl extends ServiceImpl<UserTunnelMapper, UserTunnel>
    implements UserTunnelService{

    @Override
    public List<UserTunnel> getUserTunnelByLevel(Long userId) {
        return getBaseMapper().getUserTunnelByLevel(userId);
    }
}




