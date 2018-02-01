package com.peterliu.lt.common.task;


import com.peterliu.lt.common.Asserts;
import com.peterliu.lt.common.DateUtils;
import com.peterliu.lt.common.task.quartz.QuartzTask;
import com.peterliu.lt.common.task.simple.DefaultTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by liujun on 2018/1/24.
 */
@Slf4j
public abstract class TaskLauncher {

    // 监控队列
    private static final LinkedBlockingQueue<DefaultTask> CURRENT_RUNNING_TASKS = new LinkedBlockingQueue<DefaultTask>(10000);

    /**
     * 任务启动, 并进行监控
     *
     * @param task
     */
    public static TaskMonitor send(Task task) {
        Asserts.isTrue(task instanceof DefaultTask, "Task Should Be InstanceOf Class DefaultTask");
        DefaultTask defaultTask = (DefaultTask) task;
        Asserts.isNotBlank(defaultTask.runner, "This is an empty task!");
        //添加到队列中
        putToQueue(defaultTask);
        //启动任务
        defaultTask.start();
        return new TaskMonitor(defaultTask);
    }

    /**
     * 强制中断线程组
     *
     * @param task
     * @return true-强制中断成功，false-强制中断失败，并设置软中断标志
     */
    public static boolean forceShutDown(Task task) {
        Asserts.isTrue(task instanceof DefaultTask, "Task Should Be InstanceOf Class DefaultTask");
        DefaultTask defaultTask = (DefaultTask) task;
        if (!defaultTask.forceShutDown()) {
            // 如果中断失败，则进行软中断
            defaultTask.end();
            return false;
        }
        // 强制中断成功，清理状态
        defaultTask.clearStatus();
        return true;
    }

    /**
     * 只设置结束标志，并不会强制中断
     *
     * @param task
     * @return
     */
    public static boolean shutDown(Task task){
        Asserts.isTrue(task instanceof DefaultTask, "Task Should Be InstanceOf Class DefaultTask");
        ((DefaultTask) task).end();
        return true;
    }

    /**
     * 只有simple类型的才能使用
     *
     * @param task
     * @throws InterruptedException
     */
    public static void join(Task task) throws InterruptedException {
        Asserts.isFalse(task instanceof QuartzTask, "Task Should Not Be InstanceOf Class QuartzTask");
        Asserts.isTrue(task instanceof DefaultTask, "Task Should Be InstanceOf Class DefaultTask");
        DefaultTask defaultTask = (DefaultTask) task;
        for (int i = 0; i < defaultTask.getThreadSize(); i++) {
            Thread thread = defaultTask.getThread(i);
            if(thread == null) {continue;}
            thread.join();
        }
    }

    static {
        // 启动更新线程状态的守护线程
        Task task = TaskFactory.createSelfLoop(3, -1, -1);
        task.assignMission("监控当前任务线程", new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultTask task = CURRENT_RUNNING_TASKS.poll(10, TimeUnit.MINUTES);
                    if (task != null) {
                        if (task.checkTimeOut()) {
                            // 超时强制关闭
                            log.info(String.format("TaskTimeOut, task-[%s], 当前时间：%s", task.getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
                            forceShutDown(task);
                        } else {
                            // 更新状态
                            task.updateStatus();
                        }
                        if (!task.isFinished()) {
                            // 任务还没有结束, 重新丢入队列
                            putToQueue(task);
                        }
                    }
                } catch (Exception e) {
                    ;
                }
            }
        }, true);
        //启动任务
        ((DefaultTask)task).start();
    }


    /**
     * 加入监控队列
     *
     * @param task
     */
    private static void putToQueue(DefaultTask task) {
        try {
            if (!CURRENT_RUNNING_TASKS.offer(task, 30, TimeUnit.SECONDS)) {
                // 加入失败
                throw new RuntimeException(String.format("FailToStartTaskBecauseOfFullQueue, task-[%s]", task.getName()));
            }
        } catch (Exception e) {
            // 加入失败
            throw new RuntimeException(String.format("FailToStartTaskBecauseOfFullQueue, task-[%s]", task.getName()), e);
        }
    }
}
