package com.scsdky.quartz.domain;

import java.util.Date;

/**
 * @author leomc
 * @date 2022/2/15
 * @description <巡线>
 */
public class CLosePatrol {
    private Long patrolId;
    private Date timeStart;
    private Date timeEnd;
    private Date timeCreate;

    public Long getPatrolId() {
        return patrolId;
    }

    public void setPatrolId(Long patrolId) {
        this.patrolId = patrolId;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    public Date getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(Date timeCreate) {
        this.timeCreate = timeCreate;
    }

    public CLosePatrol() {
    }
}
