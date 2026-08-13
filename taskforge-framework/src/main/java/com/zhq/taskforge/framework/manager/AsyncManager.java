package com.zhq.taskforge.framework.manager;

import com.zhq.taskforge.common.utils.Threads;
import com.zhq.taskforge.common.utils.spring.SpringUtils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  异步任务管理器
 */
public class AsyncManager {

    //1.创建静态实例对象 与 异步操作任务调度线程池
    private static AsyncManager me  = new AsyncManager();
    /**
     * 异步操作任务调度线程池
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    //2.延迟时间
    private int OPERATE_DELAY_TIME = 10;

    //3.单例模式，私有构造方法
    private AsyncManager() {}

    //4.公有me方法
    public static AsyncManager me() {
        return me;
    }

    //5.执行任务
    public void execute(TimerTask task) {
        executor.schedule(task,OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    //6.停止线程池
    public void shutdown(){
        Threads.shutdownAndAwaitTermination(executor);
    }

}
