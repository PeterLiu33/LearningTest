package com.koflance.lt.java.lambda;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by liujun on 2018/4/18.
 */
public class FileStream {

    public static void main(String[] args) {
        try (Stream<String> lines = Files.lines(Paths.get(FileStream.class.getResource("../com/peterliu/lt/java/lambda/data.txt").toURI()), Charset.defaultCharset())) {
            long count = lines.flatMap(line -> Arrays.stream(line.split(" "))).distinct().count();
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Stream<Integer> stream = Arrays.asList(1, 2, 3, 4, 5, 6).stream();
        stream.reduce(0, (x, y)-> x+y);
        List<Integer> numbers = stream.reduce(
                new ArrayList<Integer>(),
                (List<Integer> l, Integer e) -> {
                    l.add(e);
                    return l;
                }, (List<Integer> l1, List<Integer> l2) -> {
                    l1.addAll(l2);
                    return l1;
                });
    }
}
