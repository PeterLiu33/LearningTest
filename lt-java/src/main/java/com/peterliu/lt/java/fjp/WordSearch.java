package com.peterliu.lt.java.fjp;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

/**
 * 《 Java 7 Concurrency Cookbook 》 第五章案例自实现, 从文档中搜索词
 * <p>
 * Created by liujun on 2018/1/24.
 */
@AllArgsConstructor
public class WordSearch extends RecursiveTask<Integer> {

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
