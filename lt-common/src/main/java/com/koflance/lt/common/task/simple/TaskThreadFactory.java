package com.koflance.lt.common.task.simple;


import com.koflance.lt.common.DateUtils;
import com.koflance.lt.common.task.Task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 * <p>
 * Created by liujun on 2018/1/25.
 */
public class TaskThreadFactory implements ThreadFactory {

    // 名称后缀
    private String suffix = "@" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    // 任务
    private final Task task;
    // 分组
    private final ThreadGroup group;

    protected TaskThreadFactory(Task task) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.task = task;

    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                task.getName() + "#" + threadNumber.getAndIncrement() + suffix,
                0);
        t.setDaemon(task.isDaemon());
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
