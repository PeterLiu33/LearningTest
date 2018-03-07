package com.peterliu.lt.common.task;

/**
 * 任务接口
 * <p>
 * Created by liujun on 2018/1/26.
 */
public interface Task {

    /**
     * 配置任务
     *
     * @param name   任务名称 不允许为空
     * @param runner 任务具体内容
     */
    Task assignMission(String name, Runnable runner);

    /**
     * 配置任务
     *
     * @param name   任务名称 不允许为空
     * @param runner 任务具体内容
     * @param daemon 是否设置为守护线程， 默认不设置
     */
    Task assignMission(String name, Runnable runner, boolean daemon);

    /**
     * 配置任务
     *
     * @param name    任务名称 不允许为空
     * @param runner  任务具体内容
     * @param timeOut 任务超时时间，如果小于等于0，则无超时，超时会自动中断
     */
    Task assignMission(String name, Runnable runner, long timeOut);

    /**
     * 配置任务
     *
     * @param name               任务名称 不允许为空
     * @param allowConcurrentRun 是否允许并发调用任务, 默认允许
     * @param runner             任务具体内容
     */
    Task assignMission(String name, boolean allowConcurrentRun, Runnable runner);

    /**
     * 配置任务
     *
     * @param name               任务名称 不允许为空
     * @param allowConcurrentRun 是否允许并发调用任务, 默认允许
     * @param runner             任务具体内容
     * @param daemon             是否设置为守护线程， 默认不设置
     */
    Task assignMission(String name, boolean allowConcurrentRun, Runnable runner, boolean daemon);

    /**
     * 配置任务
     *
     * @param name               任务名称 不允许为空
     * @param allowConcurrentRun 是否允许并发调用任务, 默认允许
     * @param runner             任务具体内容
     * @param timeOut            任务超时时间，如果小于等于0，则无超时，超时会自动中断
     */
    Task assignMission(String name, boolean allowConcurrentRun, Runnable runner, long timeOut);

    /**
     * 任务名称
     *
     * @return
     */
    String getName();

    /**
     * 获取线程状态
     *
     * @return
     */
    Thread.State[] getStatus();

    /**
     * 获取指定序号线程实时状态
     *
     * @param index
     * @return
     */
    Thread.State getStatus(int index);

    /**
     * 任务启动线程数, 默认一个
     *
     * @return
     */
    int getThreadSize();

    /**
     * 任务超时时间，如果小于等于0，则无超时，超时会自动中断
     *
     * @return
     */
    long getTimeOut();

    /**
     * 结束标志位，true-结束
     *
     * @return
     */
    boolean isFinished();

    /**
     * 是否设置为守护线程
     *
     * @return
     */
    boolean isDaemon();

    /**
     * 增加监听器
     *
     * @param listener
     */
    void addListener(TaskListener listener);

    /**
     * 判断job是否被终止
     *
     * @return
     */
    boolean isInterrupted();
}
