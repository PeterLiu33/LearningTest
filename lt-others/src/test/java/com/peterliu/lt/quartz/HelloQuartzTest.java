package com.peterliu.lt.quartz;

import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import static org.junit.Assert.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by liujun on 2018/1/26.
 */
public class HelloQuartzTest {

    @Test
    public void run() throws Exception {
        HelloQuartz helloQuartz = new HelloQuartz();
        helloQuartz.start();
        Thread.sleep(60000);
        helloQuartz.stop();
    }

}