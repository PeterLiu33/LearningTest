package com.peterliu.lt.java.fjp;

import lombok.Getter;

import java.util.concurrent.RecursiveTask;

/**
 * 分治求取1+2+3+4的和
 * <p>
 * Created by liujun on 2018/1/24.
 */
public class Add extends RecursiveTask<Integer> {
    //父类ForkJoinTask实现了Serializable接口
    private static final long serialVersionUID = 1L;
    @Getter
    private int start;
    @Getter
    private int end;
    // 确定分治的粒度
    private static final int DIVIDE_AND_CONQUER_SIZE = 3;

    public Add(int start, int end) {
        this.end = end;
        this.start = start;
    }


    @Override
    protected Integer compute() {
        int sum = 0;
        if (end - start < DIVIDE_AND_CONQUER_SIZE) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            int middle = (start + end) / 2;
            Add left = new Add(start, middle);
            Add right = new Add(middle + 1, end);
            left.fork();
            right.fork();
            sum += left.join();
            sum += right.join();
        }
        return sum;
    }
}
