package com.koflance.lt.java.lambda;

import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Created by liujun on 2018/4/18.
 */
public class DishesStream {

    private static BiFunction<String, Integer, Dish> dishCreator = Dish::new;
    private static Predicate<Dish> colariesHigh = x -> x.getCalories() < 500;
    private static Predicate<Dish> colariesLow = x -> x.getCalories() > 300;
    private static Predicate<Dish> colariesALL = colariesHigh.and(colariesLow);


    public static void main(String[] args) {
        List<Dish> dishes = Lists.newArrayList(
                dishCreator.apply("rice", 100),
                dishCreator.apply("chocolate", 5500),
                dishCreator.apply("banana", 300),
                dishCreator.apply("pear", 350),
                dishCreator.apply("meat", 5000),
                dishCreator.apply("strawberry", 301)
        );

        // 按热量高低排序，输出实物名字
        List<String> collect = dishes.parallelStream()
                .sorted(Comparator.comparing(Dish::getCalories).reversed())
                .map(Dish::getName)
                .collect(toList());
        collect.stream().forEach(System.out::println);

        // 增加过滤规则
        collect = dishes.parallelStream().filter(colariesALL).sorted(Comparator.comparing(Dish::getCalories).reversed())
                .map(Dish::getName)
                .collect(toList());
        collect.stream().forEach(System.out::println);
    }
}
