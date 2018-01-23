package com.peterliu.lt.java;

import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * 测试FJP框架
 *
 * Created by liujun on 2018/1/22.
 */
public class TestCompletableFuture {

    @Test
    public void testLambda(){
        CompletableFuture.supplyAsync(()-> "hello world!").thenAccept((s) -> System.out.println(s));
    }

    @Test
    public void testAdd(){
        System.out.println(((ForkJoinPool)Executors.newWorkStealingPool()).submit(new Add(1,40)).join());
    }

    @Test
    public void testFibnacci(){
        Integer result = ((ForkJoinPool) Executors.newWorkStealingPool()).submit(new Fibnacci(20)).join();
        Assert.assertEquals(new Integer(6765), result);
    }

    // 分治求取1+2+3+4的和
    public static class Add extends RecursiveTask<Integer>{

        @Getter
        private int start;
        @Getter
        private int end;
        private static final int MAX_RECURSIVE_SIZE = 3;

        public Add(int start, int end){
            this.end = end;
            this.start = start;
        }


        @Override
        protected Integer compute() {
            int sum = 0;
            if(end - start < MAX_RECURSIVE_SIZE){
                for(int i = start; i <= end; i++){
                    sum += i;
                }
            }else{
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

    // 分治求取Fibnacci序列值，给定序号，求取当前序位的Fibnacci值
    public static class Fibnacci extends RecursiveTask<Integer>{

        // 存储结果
        @Getter
        private volatile int result = 0;
        // 初始化序列
        private int seq;

        public Fibnacci(int seq){
            if(seq < 0){
                throw new IllegalArgumentException("seq cannot be negative");
            }
            this.seq = seq;
        }

        @Override
        protected Integer compute() {
            if(seq <= 1){
                result += seq;
                return result;
            }else{
                Fibnacci left = new Fibnacci(seq - 1);
                Fibnacci right = new Fibnacci(seq - 2);
                // 方式一
                invokeAll(left, right);
                result = left.getResult() + right.getResult();
                // 方式二
//                left.fork();
//                right.fork();
//                result += left.join();
//                result += right.join();
                return result;
            }
        }
    }

}
