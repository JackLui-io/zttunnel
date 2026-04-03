package com.scsdky.web.service;

import com.scsdky.web.domain.UserTunnel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface UserTunnelService extends IService<UserTunnel> {

    /**
     * 获取第四层级的id 用来渲染叶子节点回显
     * @param userId
     * @return
     */
    List<UserTunnel> getUserTunnelByLevel(Long userId);
}
