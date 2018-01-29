package com.peterliu.lt.quartz;

import com.peterliu.lt.common.DateUtils;
import org.junit.Test;
import org.quartz.*;

import java.util.Date;

import static org.quartz.DateBuilder.IntervalUnit;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * Created by liujun on 2018/1/26.
 */
public class HelloHolidayCalendarTest {

    @Test
    public void test() throws SchedulerException, InterruptedException {
        JobDetail jobDetail = newJob(HelloHC.class).withIdentity("job", "koflance").build();
        Trigger trigger = newTrigger()
                .withIdentity("trigger", "koflance")
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(2)//每隔五秒调用一次
                        .repeatForever())//一直轮询)
//                .withSchedule(dailyAtHourAndMinute(2, 40))
                .startNow() // 现在开始启动
                .endAt(futureDate(10, IntervalUnit.SECOND)) // 10秒后自动结束
                .modifiedByCalendar("testHoliday") // 增加节假日
                .build();
        HelloHolidayCalendar testHoliday = new HelloHolidayCalendar();
        Date holiday = new Date();
        // 将今日设置为节假日
//        testHoliday.addHoliday(holiday);

        HelloScheduler helloScheduler = new HelloScheduler();
        // 增加商业节假日 - job不会执行
        System.out.println("将今日设置为节假日，时间是：" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
        helloScheduler.newScheduler();
        helloScheduler.addHoliday("testHoliday", testHoliday, true, true);
        helloScheduler.start(jobDetail, trigger);
        Thread.sleep(5000);
        helloScheduler.end();

        //不增加节假日 - job会执行
        System.out.println("清除节假日，时间是：" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
        testHoliday.removeHoliday(holiday);
        // 创建一个新的
        helloScheduler.newScheduler(); // scheduler被停止后，除非重新实例化，否则不能重新启动
        helloScheduler.addHoliday("testHoliday", testHoliday, true, true);
        helloScheduler.start(jobDetail, trigger);
        Thread.sleep(5000);
        helloScheduler.end();
    }

    public static class HelloHC implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("这是一个测试任务，时间是：" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
        }
    }
}