package com.koflance.lt.java.thread;

import lombok.AllArgsConstructor;

/**
 * 计算数字乘法表，来自《 Java 7 Concurrency Cookbook 》的第一章
 * <p>
 * Created by liujun on 2018/1/24.
 */
@AllArgsConstructor
public class Calculator implements Runnable {
    private int number;

    @Override
    public void run() {
        for (int i = 1; i <= 10; i++) {
            System.out.printf("%s: %d * %d = %d\n", Thread.currentThread().getName(), number, i, i * number);
        }
    }
}
