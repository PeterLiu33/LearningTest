package com.peterliu.lt.java.fjp;

import lombok.Getter;

import java.util.concurrent.RecursiveTask;

/**
 * 分治求取Fibnacci序列值，给定序号，求取当前序位的Fibnacci值
 * <p>
 * Created by liujun on 2018/1/24.
 */
public class Fibnacci extends RecursiveTask<Long> {
    //父类ForkJoinTask实现了Serializable接口
    private static final long serialVersionUID = 1L;
    // 存储结果
    @Getter
    private volatile long ret = 0;
    // 初始化序列
    private int seq;
    // 确定分治的粒度：如果问题的大小是小于既定的大小，你直接在任务中解决这问题。
    public static int DIVIDE_AND_CONQUER_SIZE = 499;

    public Fibnacci(int seq) {
        if (seq < 0) {
            throw new IllegalArgumentException("seq cannot be negative");
        }
        this.seq = seq;
    }

    // 采用多线程FJ框架分治
    @Override
    protected Long compute() {
        if (seq <= DIVIDE_AND_CONQUER_SIZE) {
            ret += runFib2(seq);
            return ret;
        } else {
            Fibnacci left = new Fibnacci(seq - 1);
            Fibnacci right = new Fibnacci(seq - 2);
            // 方式一 同步调用
            invokeAll(left, right);
            ret = left.getRet() + right.getRet();
            // 方式二
//                left.fork();
//                right.fork();
//                result += left.join();
//                result += right.join();
            return ret;
        }
    }

    // 采用单线程递归
    private long runFib(long seq) {
        if (seq <= 1) {
            return seq;
        } else {
            return runFib(seq - 1) + runFib(seq - 2);
        }
    }

    // 采用循环
    private long runFib2(long seq) {
        long left = 0;
        long right = 1;
        long temp;
        for (long i = 2; i <= seq; i++) {
            temp = right;
            right = left + right;
            left = temp;
        }
        return right;
    }
}
