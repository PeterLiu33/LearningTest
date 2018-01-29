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

    /**
     * @param threadSize 执行线程数
     * @param interval   循环间隔  小于等于0将不间隔
     * @param startDelay 启动延时 小于等于0将不延时
     */
    public SelfLoopTask(int threadSize, int interval, int startDelay) {
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.resetConfig(threadSize);
        this.interval = interval;
    }

    @Override
    protected void run(int index) {
        while (!this.isFinished()) {
            if(this.timeOut > 0) {
                if (System.currentTimeMillis() - this.startTime[index] > this.timeOut) {
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
        }
    }
}
