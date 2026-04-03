package com.scsdky.quartz.mapper;

import com.scsdky.quartz.domain.CLosePatrol;

import java.util.List;

/**
 * 调度任务信息 数据层
 *
 * @author leomc
 */
public interface ClosePatrolMapper {
    /**
     * 查询需要关闭的巡线
     */
    List<CLosePatrol> selectNeedClosePatrol();

    /**
     * 关闭巡线
     */
    Boolean updatePatrolState(CLosePatrol cLosePatrol);
}
