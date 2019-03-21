package com.koflance.lt.quartz;

import org.junit.Test;

import static org.quartz.JobBuilder.newJob;

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