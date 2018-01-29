package com.peterliu.lt.common.task.quartz;

import com.peterliu.lt.common.Asserts;
import org.quartz.ScheduleBuilder;

import java.util.Date;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.cronSchedule;

/**
 * 定时任务，使用CornTrigger实现
 * <p>
 * Created by liujun on 2018/1/26.
 */
public class CornSchedulerTask extends QuartzTask {


    // corn 表达式
    private String cornExpression;

    /**
     * @param threadSize     执行线程
     * @param startDelay     启动延时
     * @param cornExpression Corn表达式
     */
    public CornSchedulerTask(int threadSize, int startDelay, String cornExpression) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.cornExpression = cornExpression;
        this.resetConfig(threadSize);
    }

    /**
     * @param threadSize     执行线程
     * @param startDelay     启动延时
     * @param cornExpression Corn表达式
     * @param holidays       商业节假日
     */
    public CornSchedulerTask(int threadSize, int startDelay, String cornExpression, Set<Date> holidays) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.cornExpression = cornExpression;
        this.addHoliday(holidays);
        this.resetConfig(threadSize);
    }

    @Override
    protected ScheduleBuilder getScheduleBuilder() {
        return cronSchedule(cornExpression);
    }
}
