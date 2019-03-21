package com.koflance.lt.java.lambda;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Created by liujun on 2018/4/18.
 */
public class AppleFilter {

    static List<Apple> filter(List<Apple> apples, Predicate<Apple> predicate){
        List<Apple> rst = Lists.newArrayList();
        for (Apple apple : apples) {
            if(predicate.test(apple)){
                rst.add(apple);
            }
        }
        return rst;
    }

    private static BiFunction<String, String, Apple> appleBiFunction = Apple::new;

    public static void main(String[] args) {
        List<Apple> apples = Lists.newArrayList(
                appleBiFunction.apply("1", "green"),
                appleBiFunction.apply("3", "red"),
                appleBiFunction.apply("2", "blue"),
                appleBiFunction.apply("4", "green1"),
                appleBiFunction.apply("4", "green3"),
                appleBiFunction.apply("4", "green2")
        );
        // 过滤1
        List<Apple> filter = AppleFilter.filter(apples, ApplePredicate::isGreen);
        filter.stream().forEach(AppleFilter::print);
        // 过滤2
        apples.stream().filter(ApplePredicate::isGreen).forEach(AppleFilter::print);
        // 过滤3
        apples.stream().filter(x-> StringUtils.equals("green", x.getColor())).forEach(AppleFilter::print);
        // 排序1
        apples.stream().sorted((x, y) -> y.getName().compareTo(x.getName())).forEach(AppleFilter::print);
        // 排序2
        apples.stream().sorted(Comparator.comparing(Apple::getName)).forEach(AppleFilter::print);
        // 排序3
        apples.sort(Comparator.comparing(Apple::getName));
        apples.stream().forEach(AppleFilter::print);
        // 排序4
        apples.sort(Comparator.comparing(Apple::getName).reversed().thenComparing(Apple::getColor));
        apples.stream().forEach(AppleFilter::print);
    }

    static void print(Apple apple){
        System.out.println(apple);
    }
}
