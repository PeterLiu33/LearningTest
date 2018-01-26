package com.peterliu.lt.quartz;

import lombok.Setter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 基本案例，包括SimpleTrigger、JobDataMap等
 * <p>
 * Created by liujun on 2018/1/25.
 */
public class HelloQuartz {

    private Scheduler scheduler;

    public HelloQuartz() {

    }

    public void start() throws SchedulerException {
        // 读取quartz.properties配置文件
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        // DSL
        JobDetail jobDetail = newJob(HelloQuartz.HelloJob.class).withIdentity("helloWorldJob", "lt-quartz")
                // 增加状态信息
                .usingJobData("count", 1)
                .usingJobData("location", "shanghai")
                .usingJobData("name", "koflance").build();
        Trigger trigger = newTrigger()
                .withIdentity("helloWorldTrigger", "lt-quartz")
                // 注意名字与job中的一样
                .usingJobData("location", "hangzhou")
                .startNow() // 现在开始启动
                .withSchedule(
                        simpleSchedule()
                                .withIntervalInSeconds(5)//每隔五秒调用一次
                                .repeatForever())//一直轮询
                .build();

        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    public void stop() throws SchedulerException {
        if (this.scheduler != null) {
            scheduler.shutdown();
        }
    }

    // 具体业务逻辑
    // 持久化JobDetail中的JobDataMap的值
    @PersistJobDataAfterExecution
    // 同一个jobkey，不允许并发
    @DisallowConcurrentExecution
    public static class HelloJob implements Job {

        // JobFactory自动注入JobDataMap功能
        @Setter
        private String location;
        @Setter
        private String name;

        private int jobCount = 0;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("Current Scheduler: " + context.getScheduler().getSchedulerName() + " key: " + context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }


            // 是trigger和job的jobdatamap的并集
            JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
            // 只获取JobDetail
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            System.out.println("my name is " + mergedJobDataMap.getString("name"));
            System.out.println("my name is (auto inject, value from job)" + name);
            System.out.println("location is (from trigger) " + mergedJobDataMap.getString("location"));
            System.out.println("location is (from job) " + jobDataMap.getString("location"));
            System.out.println("location is (auto inject, value from trigger) " + location);


            // 增加计数， 与PersistJobDataAfterExecution注解对应，如果没有该注解，则
            int temp = jobDataMap.getInt("count");
            System.out.println("count is (success to increase)" + temp);
            jobDataMap.put("count", ++temp);
            // 此处增加无效，因为每次执行时，HelloJob都会重新实例化
            System.out.println("jobCount is (fail to increase)" + jobCount);
            jobCount++;
        }


    }
}
