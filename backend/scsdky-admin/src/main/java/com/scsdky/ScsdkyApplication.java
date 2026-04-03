package com.scsdky;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.scsdky.web.task.DeviceOfflineCheckTask;
import com.scsdky.web.task.NotificationSendTask;
import lombok.extern.slf4j.Slf4j;

/**
 * 启动程序
 *
 * @author leomc
 */
@Slf4j
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableScheduling
public class ScsdkyApplication implements CommandLineRunner
{
    @Autowired
    private DeviceOfflineCheckTask deviceOfflineCheckTask;

    @Autowired
    private NotificationSendTask notificationSendTask;

    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ScsdkyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

    /**
     * 【已关闭】应用启动后立即执行定时任务
     * 
     * 注意：设备掉线检查和通知发送功能已迁移到独立进程健康巡检服务，此处已关闭
     */
    @Override
    public void run(String... args) throws Exception {
        // 功能已迁移到独立进程，此处不再执行
        // log.info("========== 应用启动完成，立即执行定时任务 ==========");
        // 
        // // 延迟3秒后执行，确保所有Bean都已初始化完成
        // new Thread(() -> {
        //     try {
        //         Thread.sleep(3000);
        //         log.info("========== 开始执行设备掉线检查任务（启动时立即执行）==========");
        //         deviceOfflineCheckTask.executeCheckDeviceOffline();
        //         
        //         log.info("========== 开始执行通知发送任务（启动时立即执行）==========");
        //         notificationSendTask.executeSendNotifications();
        //         
        //         log.info("========== 启动时定时任务执行完成 ==========");
        //     } catch (InterruptedException e) {
        //         log.error("启动时执行定时任务被中断", e);
        //         Thread.currentThread().interrupt();
        //     } catch (Exception e) {
        //         log.error("启动时执行定时任务失败", e);
        //     }
        // }, "startup-task-runner").start();
    }
}
