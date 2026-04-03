package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelNotificationQueue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 设备掉线通知队列Mapper接口
 * 
 * @author system
 */
@Mapper
public interface TunnelNotificationQueueMapper extends BaseMapper<TunnelNotificationQueue> {
    
    /**
     * 查询待发送的通知记录
     * 
     * @param limit 限制数量
     * @return 通知记录列表
     */
    List<TunnelNotificationQueue> selectPendingNotifications(Integer limit);
}

