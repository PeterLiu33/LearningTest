package com.peterliu.lt.java;

import com.peterliu.lt.common.AssistTools;
import com.peterliu.lt.common.DateUtils;
import org.junit.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;

/**
 * 测试条件，即锁交换
 * 关键点：
 * object和condition的特征都是一样的，唯一的区别在于，一个监视器只能有一个锁交换，但condition可以多个（lock.newCondition），可以更多精细的控制
 * <p>
 * Created by liujun on 2018/1/31.
 */
public class TestCondition {

    /**
     * 关键点：
     * 1. 只有主线程调用了wait方法，子线程才会执行（因为执行wait和notify，都需要先获取监视器锁）
     * 2. 只有子线程调用了notify，且子线程释放了监视器锁，主线程才会从wait唤醒
     */
    @Test
    public void testNotify() {
        ObjectThread ta = new ObjectThread("ta");

        synchronized (ta) { // 通过synchronized(ta)获取“对象ta的同步锁”
            try {
                System.out.println(Thread.currentThread().getName() + " start ta");
                ta.start();

                System.out.println(Thread.currentThread().getName() + " block");
                ta.wait();    // 等待

                System.out.println(Thread.currentThread().getName() + " continue");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关键点：
     * 1. 必须确保所有的主线程都进入wait之后，才让子线程获取监视器
     * 2. notifyall之后，并不是同步唤醒所有主线程，而是一个唤醒后，释放监视器，另一个接着唤醒
     * <p>
     * 输出示例：
     * 线程开始：我是一号线程，时间：2018-01-31_21_48_05
     * 线程开始：我是二号线程，时间：2018-01-31_21_48_05
     * 线程开始：我是子线程，时间：2018-01-31_21_48_06
     * 线程结束：我是子线程，时间：2018-01-31_21_48_07
     * 线程结束：我是二号线程，时间：2018-01-31_21_48_09
     * 线程结束：我是一号线程，时间：2018-01-31_21_48_11
     *
     * @throws InterruptedException
     */
    @Test
    public void testNotifyAll() throws InterruptedException {
        Object monitor = new Object();// 监视器对象
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 非公平
                synchronized (monitor) {
                    out.println(AssistTools.format("线程开始：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AssistTools.sleep(2000);
                    out.println(AssistTools.format("线程结束：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                }
            }
        };
        Thread main1 = new Thread(runnable, "我是一号线程");
        Thread main2 = new Thread(runnable, "我是二号线程");
        Thread sub = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (monitor) {
                    out.println(AssistTools.format("线程开始：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                    monitor.notifyAll();
                    AssistTools.sleep(1000);
                    out.println(AssistTools.format("线程结束：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));

                }
            }
        }, "我是子线程");
        main1.start();
        main2.start();
        // 必须等待，让所有的主线程都进入wait
        AssistTools.sleep(1000);
        sub.start();
        main1.join();
        main2.join();
        sub.join();
    }

    static class ObjectThread extends Thread {

        public ObjectThread(String name) {
            super(name);
        }

        public void run() {
            synchronized (this) { // 通过synchronized(this)获取“当前对象的同步锁”
                out.println(Thread.currentThread().getName() + " wakup others");
                notify();    // 唤醒“当前对象上的等待线程”, 但main线程并不会立即执行，因为ta监视器还没有释放给ta
                AssistTools.sleep(10000);
                out.println("finished ta");
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    /**
     * 关键点：
     * 1. 只有主线程调用了await方法，子线程才会执行（因为执行await和signal，都需要先获取lock锁）
     * 2. 只有子线程调用了signal，且子线程释放了lock锁，主线程才会从await唤醒
     */
    @Test
    public void testCondition() throws InterruptedException {
        ConditionThread ta = new ConditionThread("ta");

        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " start ta");
            ta.start();

            System.out.println(Thread.currentThread().getName() + " block");
            condition.await();    // 等待

            System.out.println(Thread.currentThread().getName() + " continue");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关键点：
     * 1. 必须确保所有的主线程都进入await之后，才让子线程获取lock锁
     * 2. singalAll之后，并不是同步唤醒所有主线程，而是一个唤醒后，释放lock锁，另一个接着唤醒
     * <p>
     * 输出示例：
     * 线程开始：我是一号线程，时间：2018-02-01_10_38_21
     * 线程开始：我是二号线程，时间：2018-02-01_10_38_21
     * 线程开始：我是子线程，时间：2018-02-01_10_38_22
     * 线程结束：我是子线程，时间：2018-02-01_10_38_23
     * 线程结束：我是一号线程，时间：2018-02-01_10_38_25
     * 线程结束：我是二号线程，时间：2018-02-01_10_38_27
     *
     * @throws InterruptedException
     */
    @Test
    public void testSingalAll() throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 非公平
                lock.lock();
                try {
                    out.println(AssistTools.format("线程开始：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AssistTools.sleep(2000);
                    out.println(AssistTools.format("线程结束：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));

                } finally {
                    lock.unlock();
                }
            }
        };
        Thread main1 = new Thread(runnable, "我是一号线程");
        Thread main2 = new Thread(runnable, "我是二号线程");
        Thread sub = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    out.println(AssistTools.format("线程开始：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                    condition.signalAll();
                    AssistTools.sleep(1000);
                    out.println(AssistTools.format("线程结束：%s，时间：%s", Thread.currentThread().getName(), DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE)));
                } finally {
                    lock.unlock();
                }
            }
        }, "我是子线程");
        main1.start();
        main2.start();
        // 必须等待，让所有的主线程都进入wait
        AssistTools.sleep(1000);
        sub.start();
        main1.join();
        main2.join();
        sub.join();
    }

    // 互斥锁
    private static Lock lock = new ReentrantLock();
    // 可以创建多个condition
    private static Condition condition = lock.newCondition();

    static class ConditionThread extends Thread {
        public ConditionThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            lock.lock();
            try {
                out.println(Thread.currentThread().getName() + " wakup others");
                condition.signal();
                AssistTools.sleep(10000);
                out.println("finished ta");
            } finally {
                lock.unlock();
            }
        }
    }
}
