package com.koflance.lt.quartz;

import com.peterliu.lt.common.DateUtils;
import org.junit.Test;
import org.quartz.*;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

/**
 * 测试日历
 *
 * Created by liujun on 2018/1/26.
 */
public class HelloCornScheduler {

    @Test
    public void test() throws SchedulerException, InterruptedException {
        JobDetail jobDetail = newJob(CornJob.class)
                .withIdentity("cornJob", "koflance")
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("cornTrigger", "koflance")
                .withSchedule(cronSchedule("0/2 * * ? * *"))
                .startNow()
                .build();

        HelloScheduler helloScheduler = new HelloScheduler();
        // 创建调度器
        helloScheduler.newScheduler();
        // 启动
        helloScheduler.start(jobDetail, trigger);
        Thread.sleep(10000);
        // 终止
        helloScheduler.end();
    }

    public static class CornJob implements Job{

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("这是一个日历测试任务，时间是：" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
        }
    }
}
