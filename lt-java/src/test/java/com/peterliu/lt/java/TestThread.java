package com.peterliu.lt.java;

import com.peterliu.lt.java.thread.Calculator;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 测试Thread线程类
 * <p>
 * Created by liujun on 2018/1/24.
 */
public class TestThread {

    // 有高优先级的线程们比低优先级的先结束
    @Test
    public void testCalculator() {
        Thread threads[] = new Thread[10];
        // 保存线程状态
        Thread.State status[] = new Thread.State[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Calculator(i));
            // 设置优先级
            if (i % 2 == 0) {
                threads[i].setPriority(Thread.MAX_PRIORITY);
            } else {
                threads[i].setPriority(Thread.MIN_PRIORITY);
            }
            threads[i].setName("thread" + i);
        }
        try (FileWriter file = new FileWriter(".\\data\\log.txt"); PrintWriter pw = new PrintWriter(file);) {
            // 把线程状态的改变写入文档
            for (int i = 0; i < 10; i++) {
                pw.println("Main : Status of Thread " + i + " : " + threads[i].getState());
                status[i] = threads[i].getState();
            }
            // 执行线程
            for (int i = 0; i < 10; i++) {
                threads[i].start();
            }
            // 循环遍历，直至线程结束
            boolean finish = false;
            while (!finish) {
                for (int i = 0; i < 10; i++) {
                    if (threads[i].getState() != status[i]) {
                        writeThreadInfo(pw, threads[i], status[i]);
                        status[i] = threads[i].getState();
                    }
                }
                finish = true;
                for (int i = 0; i < 10; i++) {
                    finish = finish && (threads[i].getState() == Thread.State.TERMINATED);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeThreadInfo(PrintWriter pw, Thread thread, Thread.State state) {
        pw.printf("Main : Id %d - %s\n", thread.getId(), thread.getName());
        pw.printf("Main : Priority: %d\n", thread.getPriority());
        pw.printf("Main : Old State: %s\n", state);
        pw.printf("Main : New State: %s\n", thread.getState());
        pw.printf("Main : ************************************\n");
    }
}
