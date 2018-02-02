package com.peterliu.lt.java.queue;

import com.peterliu.lt.common.DateUtils;
import com.peterliu.lt.common.task.Task;
import com.peterliu.lt.common.task.TaskFactory;
import com.peterliu.lt.common.task.TaskLauncher;
import com.peterliu.lt.common.task.TaskMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.peterliu.lt.common.AssistTools.format;
import static java.lang.System.out;

/**
 * Created by liujun on 2018/2/1.
 */
public class BlockingQueueTest {

    @Test
    public void testBlockingQueue() throws InterruptedException {
        test(new ArrayBlockingQueue<String>(1000, false), 5000000, 5, 3);
        testJUC(new java.util.concurrent.ArrayBlockingQueue<String>(1000, false), 5000000, 5, 3);
        test(new ConcurrentArrayBlockingQueue<String>(1000, false), 5000000, 5, 3);
        testJUC(new LinkedBlockingQueue<String>(1000), 5000000, 5, 3);
    }

    public static void test(BlockingQueue<String> blockingQueue, final int size, int readSize, int writeSize) throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        Runnable write = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                for (int i = 0; i < size; ) {
                    try {
//                        out.println(name);
                        if (blockingQueue.offer(name + ":" + i)) {
                            i++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Runnable read = new Runnable() {
            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                for (; ; ) {
                    try {
                        String poll = blockingQueue.poll(50000, TimeUnit.MICROSECONDS);
                        if (poll != null) {
//                            out.println(poll);
                            count.incrementAndGet();
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Task writeTask = TaskFactory.createOneTime(writeSize, -1);
        writeTask.assignMission("writeTask", write);
        Task readTask = TaskFactory.createOneTime(readSize, -1);
        readTask.assignMission("readTask", read);
        out.println(format("starting...... date: %s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
        long start = System.currentTimeMillis();
        TaskMonitor send = TaskLauncher.send(readTask);
        send = TaskLauncher.send(writeTask);
//        out.println(send.printLog());
//        out.println(send.printLog());
        TaskLauncher.join(readTask, writeTask);
        out.println(format("total: %d, time: %s, cost: %s", count.intValue(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS), System.currentTimeMillis() - start));
        Assert.assertEquals(size * writeSize, count.get());
    }

    public static void testJUC(java.util.concurrent.BlockingQueue<String> blockingQueue, final int size, int readSize, int writeSize) throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        Runnable write = new Runnable() {
            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                for (int i = 0; i < size; ) {
                    try {
                        if (blockingQueue.offer(name + ":" + i)) {
                            i++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Runnable read = new Runnable() {
            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                for (; ; ) {
                    try {
                        String poll = blockingQueue.poll(50000, TimeUnit.MICROSECONDS);
                        if (poll != null) {
//                            out.println(poll);
                            count.incrementAndGet();
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Task writeTask = TaskFactory.createOneTime(writeSize, -1);
        writeTask.assignMission("writeTask", write);
        Task readTask = TaskFactory.createOneTime(readSize, -1);
        readTask.assignMission("readTask", read);
        out.println(format("starting...... date: %s", DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS)));
        long start = System.currentTimeMillis();
        TaskLauncher.send(writeTask);
        TaskLauncher.send(readTask);
        TaskLauncher.join(readTask, writeTask);
        out.println(format("total: %d, time: %s, cost: %s", count.intValue(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS), System.currentTimeMillis() - start));
        Assert.assertEquals(size * writeSize, count.get());
    }
}