package com.peterliu.lt.common.task.quartz;

import com.google.common.collect.Sets;
import com.peterliu.lt.common.Asserts;
import com.peterliu.lt.common.task.Task;
import com.peterliu.lt.common.task.simple.DefaultTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.listeners.SchedulerListenerSupport;

import java.nio.channels.ClosedByInterruptException;
import java.util.Date;
import java.util.Set;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by liujun on 2018/1/27.
 */
@Slf4j
public abstract class QuartzTask extends DefaultTask {

    // 定义Group名字
    private static final String QUARTZ_GRUOP_NAME = "quartz_task";
    private static final String QUARTZ_JOB_PREFIX = "quartz_job_";
    private static final String QUARTZ_TRIGGER_PREFIX = "quartz_trigger_";

    // 任务列表
    protected volatile JobDetail jobDetail = null;
    protected volatile Boolean triggerFinished[] = new Boolean[threadSize];
    public volatile Trigger triggers[] = new Trigger[threadSize];
    // 商业节假日期
    private Set<Date> holidaySet = Sets.newHashSet();
    // 调度器
    protected Scheduler scheduler;

    /**
     * QUARTZ不支持daemon参数
     *
     * @param name
     * @param runner
     * @param daemon
     * @return
     */
    @Override
    public Task assignMission(String name, Runnable runner, boolean daemon) {
        throw new RuntimeException("unsupported function");
    }

    //:内部方法////////////////////////////////////////////////////////////////

    /**
     * 增加商业节假日
     *
     * @param holidaySet
     */
    protected void addHoliday(Set<Date> holidaySet) {
        this.holidaySet.addAll(holidaySet);
    }

    @Override
    protected void run(int index) {
        while (true) {
            // 意外中断可重启
            try {
                // 执行真正的业务逻辑
                this.runner.run();
                break;
            } catch (Exception e) {
                if (e instanceof InterruptedException ||
                        e instanceof ClosedByInterruptException ||
                        e instanceof SecurityException) {
                    // 线程被强制中断，例如调用forceShutDown方法
                    break;
                } else {
                    // 线程意外中断, 会恢复
                    log.error(String.format("ThreadThrowException, thread-name:[%s]", Thread.currentThread().getName()), e);
                }
            }
        }
    }

    /**
     * 设置任务调度计划
     *
     * @return
     */
    protected abstract ScheduleBuilder getScheduleBuilder();

    @Override
    public void start() {
        if (!finished) {
            //线程还在运行，无法再次启动
            return;
        }
        this.finished = false;
        this.clearStatus();
        SchedulerFactory schedulerFactory = new SchedulerFactory(this);
        // 创建一个新的scheduler
        this.scheduler = schedulerFactory.newScheduler();
        JobDetail jobDetail = null;
        Trigger trigger = null;
        try {
            // 增加商业节假日
            HolidayCalendar cal = new HolidayCalendar();
            if (!this.holidaySet.isEmpty()) {
                for (Date date : this.holidaySet) {
                    cal.addExcludedDate(date);
                }
            }
            this.scheduler.addCalendar(QUARTZ_GRUOP_NAME, cal, true, true);
            // 共用JOB
            if (this.allowConcurrentRun) {
                // 允许并发
                jobDetail = newJob(ConcurrentQJob.class)
                        .withIdentity(QUARTZ_JOB_PREFIX + this.getName(), QUARTZ_GRUOP_NAME)
                        .storeDurably()// 没有trigger时，也保留
                        .build();
            } else {
                // 不允许并发
                jobDetail = newJob(NonConcurrentQJob.class)
                        .withIdentity(QUARTZ_JOB_PREFIX + this.getName(), QUARTZ_GRUOP_NAME)
                        .storeDurably()// 没有trigger时，也保留
                        .build();
            }
            // 增加监听器
            scheduler.getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {
                @Override
                public void schedulerShutdown() {
                    // 调度器关闭
                    QuartzTask.this.finished = true;
                    QuartzTask.this.notifyStop();
                }

                @Override
                public void schedulerStarted() {
                    QuartzTask.this.notifyStart();
                }

                @Override
                public void triggerFinalized(Trigger trigger) {
                    // 调度计划关闭
                    if (QuartzTask.this.finished) {
                        //已经关闭
                        return;
                    }
                    String name = trigger.getKey().getName();
                    String index = StringUtils.substringAfter(name, "#");
                    Integer integer = Integer.valueOf(index);
                    if (integer != null) {
                        QuartzTask.this.triggerFinished[integer] = true;
                    }
                    boolean temp = true;
                    for (Boolean status : QuartzTask.this.triggerFinished) {
                        temp = temp && status;
                    }
                    QuartzTask.this.finished = temp;
                }
            });
            // 增加job
            scheduler.addJob(jobDetail, true);
            // 启动服务
            scheduler.start();
            Date endDate = null;
            if(this.timeOut > 0){
                // 增加结束时间
                if(this.startDelay > 0){
                    endDate = new Date(System.currentTimeMillis() + this.startDelay + this.timeOut);
                }else {
                    endDate = new Date(System.currentTimeMillis() + this.timeOut);
                }
            }
            for (int i = 0; i < threadSize; i++) {
                if (this.startDelay > 0) {
                    // 启动延时
                    trigger = newTrigger()
                            .withIdentity(QUARTZ_TRIGGER_PREFIX + this.getName() + "#" + i, QUARTZ_GRUOP_NAME)
                            .startAt(new Date(System.currentTimeMillis() + this.startDelay))
                            .withSchedule(getScheduleBuilder())
                            .endAt(endDate) // 增加结束时间
                            .modifiedByCalendar(QUARTZ_GRUOP_NAME) // 增加商业节假日
                            .forJob(jobDetail)
                            .build();
                } else {
                    trigger = newTrigger()
                            .withIdentity(QUARTZ_TRIGGER_PREFIX + this.getName() + "#" + i, QUARTZ_GRUOP_NAME)
                            .startNow()
                            .withSchedule(getScheduleBuilder())
                            .endAt(endDate) // 增加结束时间
                            .modifiedByCalendar(QUARTZ_GRUOP_NAME) // 增加商业节假日
                            .forJob(jobDetail)
                            .build();
                }
                this.writeLock().lock();
                try {
                    this.jobDetail = jobDetail;
                    this.triggers[i] = trigger;
                    if (this.startDelay > 0) {
                        // 启动延时
                        this.startTime[i] = System.currentTimeMillis() + this.startDelay;
                    } else {
                        this.startTime[i] = System.currentTimeMillis();
                    }
                } finally {
                    this.writeLock().unlock();
                }
                this.scheduler.scheduleJob(trigger);
//                this.scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (Exception e) {
            // 启动失败
            // 程序结束
            this.finished = true;
            this.clearStatus();
            throw new RuntimeException(String.format("FailToStartQuartzService! task: [%s]", getName()), e);
        }
    }

    @Override
    public boolean forceShutDown() {
        Asserts.isNotBlank(this.scheduler, "PleaseMakeSureTaskIsInitialized!");
        boolean runStatus = true;
        this.readLock().lock();
        try {
            this.scheduler.shutdown();
            log.info(String.format("QuartzSchedulerHasForceShutDown, Name:[%s]", this.scheduler.getSchedulerName()));
        } catch (SchedulerException e) {
            // 关闭失败
            runStatus = false;
            try {
                log.error(String.format("QuartzSchedulerHasFailedToForceShutDown, Name:[%s]", this.scheduler.getSchedulerName()), e);
            } catch (SchedulerException e1) {
                log.error(String.format("QuartzSchedulerHasFailedToForceShutDown, Task:[%s]", getName()), e1);
            }
        } finally {
            this.readLock().unlock();
        }
        return this.finished = runStatus;
    }

    /**
     * 重新设置线程队列大小, 只有finished=true时才能生效
     *
     * @param threadSize
     * @return true-设置成功,false-设置失败
     */
    @Override
    protected boolean resetConfig(int threadSize) {
        if (this.finished) {
            if (this.threadSize != threadSize) {
                this.writeLock().lock();
                try {
                    this.threadSize = threadSize;
                    this.jobDetail = null;
                    this.triggers = new Trigger[threadSize];
                    this.triggerFinished = new Boolean[threadSize];
                    for (int i = 0; i < this.triggerFinished.length; i++) {
                        this.triggerFinished[i] = false;
                    }
                    this.startTime = new Long[threadSize];
                    this.status = new Thread.State[threadSize];
                } finally {
                    this.writeLock().unlock();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 清理状态
     */
    @Override
    public void clearStatus() {
        this.writeLock().lock();
        try {
            this.jobDetail = null;
            this.triggers = new Trigger[threadSize];
            this.triggerFinished = new Boolean[threadSize];
            for (int i = 0; i < this.triggerFinished.length; i++) {
                this.triggerFinished[i] = false;
            }
            this.startTime = new Long[threadSize];
            this.status = new Thread.State[threadSize];
        } finally {
            this.writeLock().unlock();
        }
    }

    @Override
    public Thread.State getStatus(int index) {
        Asserts.isTrue(index < this.threadSize, "[%d]IndexShouldBe[0,%d]", index, this.threadSize);
        Asserts.isNotBlank(this.triggers[index], "[%d]CurrentTriggerIsEmpty!", index);
        Trigger trigger = this.triggers[index];
        try {
            Trigger.TriggerState triggerState = this.scheduler.getTriggerState(trigger.getKey());
            switch (triggerState) {
                case BLOCKED:
                    return Thread.State.BLOCKED;
                case NORMAL:
                    return Thread.State.TIMED_WAITING;
                case PAUSED:
                    return Thread.State.TIMED_WAITING;
                case ERROR:
                case COMPLETE:
                    return Thread.State.TERMINATED;
                case NONE:
                    return Thread.State.NEW;
            }
        } catch (SchedulerException e) {
            log.error("FailToGetTriggerStatus", e);
            return null;
        }
        return null;
    }

    @Override
    public void updateStatus() {
        this.writeLock().lock();
        try {
            for (int i = 0; i < this.threadSize; i++) {
                if (this.triggers[i] != null) {
                    this.status[i] = getStatus(i);
                }
            }
        } finally {
            this.writeLock().unlock();
        }
    }
}
