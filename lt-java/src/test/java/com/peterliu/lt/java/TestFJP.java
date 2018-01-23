package com.peterliu.lt.java;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Random;
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
    public static void monitorTask(ForkJoinPool pool, ForkJoinTask<?> task){
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

    //:内部类///////////////////////////////////////////////////////


    // 分治求取1+2+3+4的和
    public static class Add extends RecursiveTask<Integer> {
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

    // 分治求取Fibnacci序列值，给定序号，求取当前序位的Fibnacci值
    public static class Fibnacci extends RecursiveTask<Long> {
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

    //《 Java 7 Concurrency Cookbook 》 第五章案例自实现, 更新产品列表的价格
    @AllArgsConstructor
    public static class PriceModified extends RecursiveAction {

        //父类ForkJoinTask实现了Serializable接口
        private static final long serialVersionUID = 1L;

        private List<Product> products;
        private int first;
        private int last;
        // 存储价格的增长
        private double increment;
        // 任务粒度分割线
        private static final int DIVIDE_AND_CONQUER_SIZE = 10;

        @Override
        protected void compute() {
            if (last - first < DIVIDE_AND_CONQUER_SIZE) {
                updatePrice();
            } else {
                int middle = (last + first) / 2;
                PriceModified left = new PriceModified(products, first, middle + 1, increment);
                PriceModified right = new PriceModified(products, middle + 1, last, increment);
                // 同步调用
                invokeAll(left, right);
            }
        }

        // 更新价格
        private void updatePrice() {
            for (int i = first; i < last; i++) {
                products.get(i).setPrice(products.get(i).getPrice() + increment);
            }
        }


        //:内部类///////////////////////////////////////////////////////

        // 产品类
        @Data
        @AllArgsConstructor
        public static class Product {
            private String name;
            private double price;
        }

        // 产品制造类
        public static class ProductListGenerator {
            public List<Product> generate(int size) {
                List<Product> ret = Lists.newArrayList();
                for (int i = 0; i < size; i++) {
                    ret.add(new Product("Product" + i, 10));
                }
                return ret;
            }
        }
    }

    // 《 Java 7 Concurrency Cookbook 》 第五章案例自实现, 从文档中搜索词
    @AllArgsConstructor
    public static class WordSearch extends RecursiveTask<Integer> {

        //父类ForkJoinTask实现了Serializable接口
        private static final long serialVersionUID = 1L;

        private String document[][];
        private int start;
        private int end;
        private String word;
        // 任务粒度分割线
        private static final int DIVIDE_AND_CONQUER_SIZE = 10;

        @Override
        protected Integer compute() {
            if (end - start < DIVIDE_AND_CONQUER_SIZE) {
                return processLines(document, start, end, word);
            } else {
                int middle = (end + start) / 2;
                WordSearch left = new WordSearch(document, start, middle, word);
                WordSearch right = new WordSearch(document, middle, end, word);
                invokeAll(left, right);
                try {
                    return left.get() + right.get();
                } catch (InterruptedException | ExecutionException e) {
                    return 0;
                }
            }
        }

        // 处理多行情况
        private Integer processLines(String document[][], int start, int end, String word) {
            List<WordSearchOnLine> wordSearchOnLines = Lists.newArrayList();
            for (int i = start; i < end; i++) {
                WordSearchOnLine lineTask = new WordSearchOnLine(document[i], 0, document[i].length, word);
                wordSearchOnLines.add(lineTask);
            }
            invokeAll(wordSearchOnLines);
            int ret = 0;
            for (WordSearchOnLine line : wordSearchOnLines) {
                try {
                    ret += line.get();
                } catch (InterruptedException | ExecutionException e) {
                    ;
                }
            }
            return ret;
        }

        //:内部类///////////////////////////////////////////////////////


        public static class Document {
            private String words[] = {"the", "hello", "goodbye", "packt", "java", "thread", "pool", "random", "class", "main"};

            /**
             * 返回一个字符串二维数组，来表示将要查找的单词。
             *
             * @param numLines 行数
             * @param numWords 每行单词数
             * @param word     待查找的单词
             * @return
             */
            public String[][] generateDocument(int numLines, int numWords, String word) {
                int counter = 0;
                String document[][] = new String[numLines][numWords];
                Random random = new Random();
                for (int i = 0; i < numLines; i++) {
                    for (int j = 0; j < numWords; j++) {
                        int index = random.nextInt(words.length);
                        document[i][j] = words[index];
                        if (document[i][j].equals(word)) {
                            // 指定词在整个文章中出现的次数
                            counter++;
                        }
                    }
                }
                System.out.println("DocumentMock: The word appears " + counter + " times in the document");
                return document;
            }
        }

        // 按行搜索
        @AllArgsConstructor
        public static class WordSearchOnLine extends RecursiveTask<Integer> {
            //父类ForkJoinTask实现了Serializable接口
            private static final long serialVersionUID = 1L;
            private String line[];
            private int start;
            private int end;
            // 待搜索的词
            private String word;
            // 任务粒度分割线
            private static final int DIVIDE_AND_CONQUER_SIZE = 100;


            @Override
            protected Integer compute() {
                if (end - start < DIVIDE_AND_CONQUER_SIZE) {
                    return countLine(line, start, end, word);
                } else {
                    // 分治
                    int middle = (start + end) / 2;
                    WordSearchOnLine left = new WordSearchOnLine(line, start, middle, word);
                    WordSearchOnLine right = new WordSearchOnLine(line, middle, end, word);
                    // 同步等待
                    invokeAll(left, right);
                    try {
                        return left.get() + right.get();
                    } catch (InterruptedException | ExecutionException e) {
                        return 0;
                    }
                }
            }

            // 统计单行匹配情况
            private Integer countLine(String[] line, int start, int end, String word) {
                int count = 0;
                for (int i = start; i < end; i++) {
                    if (StringUtils.equalsIgnoreCase(word, line[i])) {
                        count++;
                    }
                }
                return count;
            }
        }
    }
}
