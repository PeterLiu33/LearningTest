package com.peterliu.lt.common.task;

import com.peterliu.lt.common.task.quartz.CornSchedulerTask;
import com.peterliu.lt.common.task.quartz.SimpleSchedulerTask;
import com.peterliu.lt.common.task.simple.OneTimeTask;
import com.peterliu.lt.common.task.simple.SelfLoopTask;
import org.quartz.SimpleScheduleBuilder;

import java.util.Date;
import java.util.Set;

/**
 * Created by liujun on 2018/1/24.
 */
public abstract class TaskFactory {

    /**
     * 创建循环任务
     *
     * @param threadSize 线程组大小，必须大于0
     * @param interval   循环间隔，单位毫秒
     * @param startDelay 启动延时，单位毫秒
     * @return
     */
    public static Task createSelfLoop(int threadSize, int interval, int startDelay) {
        return new SelfLoopTask(threadSize, interval, startDelay);
    }

    /**
     * 创建一次性任务
     *
     * @param threadSize 线程组大小，必须大于0
     * @param startDelay 启动延时，单位毫秒
     * @return
     */
    public static Task createOneTime(int threadSize, int startDelay) {
        return new OneTimeTask(threadSize, startDelay);
    }

    /**
     * @param threadSize     执行线程数
     * @param startDelay     启动延时
     * @param cornExpression Corn表达式
     */
    public static Task createCornScheduler(int threadSize, int startDelay, String cornExpression) {
        return new CornSchedulerTask(threadSize, startDelay, cornExpression);
    }

    /**
     * @param threadSize     执行线程数
     * @param startDelay     启动延时
     * @param cornExpression Corn表达式
     * @param holidays       商业节假日
     */
    public static Task createCornScheduler(int threadSize, int startDelay, String cornExpression, Set<Date> holidays) {
        return new CornSchedulerTask(threadSize, startDelay, cornExpression, holidays);
    }

    /**
     * @param threadSize      执行线程数
     * @param startDelay      启动延时
     * @param scheduleBuilder Simple触发器
     */
    public static Task createSimpleScheduler(int threadSize, int startDelay, SimpleScheduleBuilder scheduleBuilder) {
        return new SimpleSchedulerTask(threadSize, startDelay, scheduleBuilder);
    }

    /**
     * @param threadSize      执行线程数
     * @param startDelay      启动延时
     * @param scheduleBuilder Simple触发器
     * @param holidays        商业节假日
     */
    public static Task createSimpleScheduler(int threadSize, int startDelay, SimpleScheduleBuilder scheduleBuilder, Set<Date> holidays) {
        return new SimpleSchedulerTask(threadSize, startDelay, scheduleBuilder, holidays);
    }

}
