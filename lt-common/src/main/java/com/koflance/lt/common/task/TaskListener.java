package com.koflance.lt.common.task;

/**
 * 监听器
 * <p>
 * Created by liujun on 2018/1/26.
 */
public interface TaskListener {

    /**
     * 任务启动
     *
     * @param task
     */
    void start(Task task);

    /**
     * 任务退出
     *
     * @param task
     */
    void stop(Task task);
}
