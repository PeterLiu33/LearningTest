package com.koflance.lt.quartz;

import com.peterliu.lt.common.Asserts;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by liujun on 2018/1/26.
 */
public class HelloScheduler {

    private Scheduler scheduler;

    public void newScheduler() throws SchedulerException {
        // 读取quartz.properties配置文件
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
    }

    public void start() throws SchedulerException {
        Asserts.isNotBlank(this.scheduler, "请先调用newScheduler()!");
        scheduler.start();
    }

    public void start(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        Asserts.isNotBlank(this.scheduler, "请先调用newScheduler()!");
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    public void addJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        Asserts.isNotBlank(this.scheduler, "请先调用newScheduler()!");
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void addHoliday(String calName, HelloHolidayCalendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException {
        Asserts.isNotBlank(this.scheduler, "请先调用newScheduler()!");
        scheduler.addCalendar(calName, calendar.getCal(), replace, updateTriggers);
    }

    public void end() throws SchedulerException {
        Asserts.isNotBlank(this.scheduler, "请先调用newScheduler()!");
        this.scheduler.shutdown();
    }
}
