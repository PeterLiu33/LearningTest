package com.peterliu.lt.common.task.simple;


import com.peterliu.lt.common.Asserts;

/**
 * 一次性任务
 *
 * Created by liujun on 2018/1/24.
 */
public class OneTimeTask extends DefaultTask {

    /**
     * @param threadSize      执行线程数
     * @param startDelay      启动延时
     */
    public OneTimeTask(int threadSize, int startDelay){
        Asserts.isTrue(threadSize > 0, "[%d]ThreadSizeCannotBeZeroOrNegative!", threadSize);
        this.startDelay = startDelay;
        this.resetConfig(threadSize);
    }

    @Override
    protected void run(int index) {
        this.runner.run();
        // 结束任务
        this.allFinished.set(index, true);
    }
}
