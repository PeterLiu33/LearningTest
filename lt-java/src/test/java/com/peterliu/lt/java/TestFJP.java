package com.peterliu.lt.java;

import com.peterliu.lt.java.fjp.Add;
import com.peterliu.lt.java.fjp.Fibnacci;
import com.peterliu.lt.java.fjp.PriceModified;
import com.peterliu.lt.java.fjp.WordSearch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

/**
 * 测试FJP框架
 * <p>
 * Created by liujun on 2018/1/22.
 */
public class TestFJP {

    @Test
    public void testLambda() {
        CompletableFuture.supplyAsync(() -> "hello world!").thenAccept((s) -> System.out.println(s));
    }

    @Test
    public void testAdd() {
        System.out.println(((ForkJoinPool) Executors.newWorkStealingPool()).submit(new Add(1, 40)).join());
    }

    /**
     * 递归分治性能其实不如循环
     */
    @Test
    public void testFibnacci() {
        Long result = ((ForkJoinPool) Executors.newWorkStealingPool()).submit(new Fibnacci(500)).join();
        Assert.assertEquals(new Long(2171430676560690477L), result);
    }

    /**
     * 更新产品列表的价格
     */
    @Test
    public void testPriceModified() {
        PriceModified.ProductListGenerator productListGenerator = new PriceModified.ProductListGenerator();
        List<PriceModified.Product> products = productListGenerator.generate(10000);
        PriceModified task = new PriceModified(products, 0, products.size(), 9);
        // 创建ForkJoinPool池
        ForkJoinPool pool = (ForkJoinPool) Executors.newWorkStealingPool();
        // 执行任务 异步调用  使用了work-stealing算法
        pool.execute(task);
        monitorTask(pool, task);
        // 关闭池子
        pool.shutdown();
        for (int i = 0; i < products.size(); i++) {
            PriceModified.Product product = products.get(i);
            if (product.getPrice() != 19) {
                System.out.printf("Product %s: %f\n", product.getName(), product.getPrice());
            }
        }
        System.out.println("Main: End of the program.\n");
    }

    // 这个案例比较清楚
    @Test
    public void testWordSearch() {
        // 1. 创建FJT
        String[][] document = new WordSearch.Document().generateDocument(1000, 500, "hello");
        WordSearch task = new WordSearch(document, 0, 1000, "hello");
        // 2. 创建ForkJoinPool池
        ForkJoinPool pool = (ForkJoinPool) Executors.newWorkStealingPool();
        // 3. 执行Task
        pool.execute(task);
        // 4. 监控Task
        monitorTask(pool, task);
        // 5. 获取结果值
        System.out.println("Document Search Result: The word appears " + task.join() + " times in the document");
        // 6. 关闭池子
        pool.shutdown();
    }

    /**
     * 监控任务执行情况
     *
     * @param pool
     * @param task
     */
    public static void monitorTask(ForkJoinPool pool, ForkJoinTask<?> task) {
        do {
            if (task.isCompletedNormally()) {
                //任务完成时有出错
                System.out.printf("Main: The process has completed normally.\n");
            }
            System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //判断任务是否执行完毕
        } while (!task.isDone());
    }
}
