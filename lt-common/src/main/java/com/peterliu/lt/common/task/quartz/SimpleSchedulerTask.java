package com.peterliu.lt.common.task.quartz;

import com.peterliu.lt.common.Asserts;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import java.util.Date;
import java.util.Set;

/**
 * 定时任务, 使用Quartz的SimpleTrigger实现
 * <p>
 * Created by liujun on 2018/1/25.
 */
public class SimpleSchedulerTask extends QuartzTask {


    SimpleScheduleBuilder scheduleBuilder;

    /**
     * @param threadSize      执行线程数
     * @param startDelay      启动延时
     * @param scheduleBuilder Simple触发器
     */
    public SimpleSchedulerTask(int threadSize, int startDelay, SimpleScheduleBuilder scheduleBuilder) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.resetConfig(threadSize);
        this.scheduleBuilder = scheduleBuilder;
    }

    /**
     * @param threadSize      执行线程数
     * @param startDelay      启动延时
     * @param scheduleBuilder Simple触发器
     * @param holidays       商业节假日
     */
    public SimpleSchedulerTask(int threadSize, int startDelay, SimpleScheduleBuilder scheduleBuilder, Set<Date> holidays) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.resetConfig(threadSize);
        this.scheduleBuilder = scheduleBuilder;
        this.addHoliday(holidays);
    }

    @Override
    protected ScheduleBuilder getScheduleBuilder() {
        return scheduleBuilder;
    }
}
