package com.scsdky.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时任务线程池配置
 * 用于配置@Scheduled定时任务的线程池，使多个定时任务可以并发执行
 * 
 * @author system
 */
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    /**
     * 定时任务线程池大小
     * 建议设置为定时任务数量的2-3倍，确保任务不会相互阻塞
     */
    private static final int POOL_SIZE = 10;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(POOL_SIZE, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "scheduled-task-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        });
    }
}

