package com.scsdky.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时任务调度测试
 *
 * @author leomc
 */
@Component("sysTask")
public class SysTask {
//    @Autowired
//    ISendRequestService iSendRequestService;

    public void sysParams(String params) {
        System.out.println("执行有参方法：" + params);
    }

    public void sysNoParams() {
        System.out.println("执行无参方法");
    }

//    public void getTheLandWarehouse() {
//        iSendRequestService.sendRequestService(0,1);
//    }

}
