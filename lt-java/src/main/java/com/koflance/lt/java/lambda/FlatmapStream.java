package com.koflance.lt.java.lambda;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by liujun on 2018/4/18.
 */
public class FlatmapStream {

    public static void main(String[] args) {
        String[] test = {"vegetable", "strawberry", "bork", "chicken", "beef"};
        Stream<String> stream = Arrays.stream(test);
        stream.map(x->x.split("")).collect(toList()).stream().map(x->StringUtils.join(x, ",")).forEach(System.out::println);
        stream = Arrays.stream(test);
        List<String> collect = stream.map(x -> x.split("")).flatMap(Arrays::stream).distinct().collect(toList());
        System.out.println(StringUtils.join(collect, ","));
        stream = Arrays.stream(test);
        Set<String> stringSet = stream.map(x -> x.split("")).flatMap(Arrays::stream).collect(toSet());
        System.out.println(StringUtils.join(stringSet, ","));
        stream = Arrays.stream(test);
        stringSet = stream.map(x -> x.split("")).flatMap(Arrays::stream).collect(toCollection(LinkedHashSet::new));
        System.out.println(StringUtils.join(stringSet, ","));

        // 这是因为IntStream 中的map方法只能为流中的每个元素返回另一个int
        int[] a = {1, 2, 3};
        int[] b = {3, 4};
        IntStream astream = Arrays.stream(a);
        // 装箱操作
        List<int[]> collect1 = astream.boxed().flatMap(i -> Arrays.stream(b).mapToObj(j -> new int[]{i, j})).collect(toList());
        System.out.println(StringUtils.join(collect1, ","));


        List<Integer> numbers1 = Arrays.asList(1, 2, 3);
        List<Integer> numbers2 = Arrays.asList(3, 4);
        List<int[]> pairs =
                numbers1.stream()
                        .flatMap(i -> numbers2.stream()
                                .map(j -> new int[]{i, j})
                        )
                        .collect(toList());
        System.out.println(StringUtils.join(pairs, ","));
        long ab = 1_000;
        System.out.println(ab);

    }
}
