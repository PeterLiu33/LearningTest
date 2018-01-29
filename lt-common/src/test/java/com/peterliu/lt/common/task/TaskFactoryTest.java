package com.peterliu.lt.common.task;

import com.peterliu.lt.common.DateUtils;
import org.junit.Test;
import org.quartz.SimpleScheduleBuilder;

import static org.junit.Assert.*;

/**
 * Created by liujun on 2018/1/29.
 */
public class TaskFactoryTest {
    @Test
    public void createSelfLoop() throws Exception {
        Task task = TaskFactory.createSelfLoop(2, 3000, 1000);
        task.assignMission("我是一个循环测试任务", true, new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("当前时间：%s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println(String.format("线程：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
            }
        }, 10000);
        task.addListener(new TaskListener() {
            @Override
            public void start(Task task) {
                System.out.println("任务启动");
            }

            @Override
            public void stop(Task task) {
                System.out.println("任务关闭");
            }
        });
        TaskMonitor monitor = TaskLauncher.send(task);
        System.out.println(monitor.printLog());
        Thread.sleep(15000);
        System.out.println(monitor.printLog());
        TaskLauncher.forceShutDown(task);
        System.out.println(monitor.printLog());
    }

    @Test
    public void createOneTime() throws Exception {
        Task task = TaskFactory.createOneTime(2, 1000);
        task.assignMission("我是一个一次性测试任务", new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("当前时间：%s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println(String.format("线程：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
            }
        }, 4000);
        TaskMonitor monitor = TaskLauncher.send(task);
        System.out.println(monitor.printLog());
        Thread.sleep(5000);
        System.out.println(monitor.printLog());
        TaskLauncher.forceShutDown(task);
        System.out.println(monitor.printLog());
    }

    @Test
    public void createCornScheduler() throws Exception {
        Task task = TaskFactory.createCornScheduler(2, 1000, "0/2 * * ? * *");
        task.assignMission("我是一个日历测试任务", new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("当前时间：%s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println(String.format("线程：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
            }
        }, 10000);
        TaskMonitor monitor = TaskLauncher.send(task);
        System.out.println(monitor.printLog());
        Thread.sleep(15000);
        System.out.println(monitor.printLog());
        TaskLauncher.forceShutDown(task);
        System.out.println(monitor.printLog());
    }

    @Test
    public void createSimpleScheduler() throws Exception {
        Task task = TaskFactory.createSimpleScheduler(2, 1000, SimpleScheduleBuilder.simpleSchedule().repeatForever().withIntervalInSeconds(1));
        task.assignMission("我是一个简单循环测试任务", new Runnable() {
            @Override
            public void run() {
                System.out.println(String.format("当前时间：%s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println(String.format("线程：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
            }
        });
        task.addListener(new TaskListener() {
            @Override
            public void start(Task task) {
                System.out.println("任务启动");
            }

            @Override
            public void stop(Task task) {
                System.out.println("任务关闭");
            }
        });
        TaskMonitor monitor = TaskLauncher.send(task);
        System.out.println(monitor.printLog());
        Thread.sleep(15000);
        System.out.println(monitor.printLog());
        TaskLauncher.forceShutDown(task);
        System.out.println(monitor.printLog());
    }

}