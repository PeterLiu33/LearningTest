package com.peterliu.lt.common.task.simple;


import com.peterliu.lt.common.Asserts;

/**
 * 自循环任务
 * <p>
 * Created by liujun on 2018/1/24.
 */

public class SelfLoopTask extends DefaultTask {


    // 任务每次循环间间隔
    private int interval = 10;
    // 循环次数，默认不限制
    private final int numberOfCycle;

    /**
     * @param threadSize 执行线程数
     * @param interval   循环间隔  小于等于0将不间隔
     * @param startDelay 启动延时 小于等于0将不延时
     */
    public SelfLoopTask(int threadSize, int interval, int startDelay) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.numberOfCycle = -1;
        this.resetConfig(threadSize);
        this.interval = interval;
    }

    /**
     * @param threadSize    执行线程数
     * @param interval      循环间隔  小于等于0将不间隔
     * @param numberOfCycle 循环次数，默认不限制
     * @param startDelay    启动延时 小于等于0将不延时
     */
    public SelfLoopTask(int threadSize, int interval, int numberOfCycle, int startDelay) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.numberOfCycle = numberOfCycle;
        this.resetConfig(threadSize);
        this.interval = interval;
    }


    @Override
    protected void run(int index) {
        int cycleTimes = this.numberOfCycle;
        while (!this.isFinished() && !isJobInterrupted()) {
            // 计数到了或者被中断了
            if (this.timeOut > 0) {
                if (System.currentTimeMillis() - this.startTime.get(index) > this.timeOut) {
                    // 超时
                    this.finished = true;
                    break;
                }
            }
            this.runner.run();
            if (this.interval > 0) {
                // 循环间隔
                try {
                    Thread.sleep(this.interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (this.numberOfCycle > 0 && --cycleTimes <= 0) {
                // 终止本线程
                this.allFinished.set(index, true);
                break;
            }
        }
    }
}
