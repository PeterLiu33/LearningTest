package com.koflance.lt.common;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by liujun on 2018/1/31.
 */
public class AssistTools {

    /**
     * 辅助格式化工具
     *
     * @param format
     * @param args
     * @return
     */
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }

    /**
     * 暂停线程
     *
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNotBlank(String... args) {
        return args != null && !Arrays.stream(args).filter(StringUtils::isBlank).map(x -> "").findAny().isPresent();
    }

    public static Predicate<Object[]> arrayNotBlank = (array) -> array != null && array.length > 0;
    public static Predicate<Object[]> arrayIsBlank = (array) -> array == null || array.length == 0;
    public static BiPredicate<Object[], Integer> arrayMatchLength = (array, length) -> array != null && array.length == length;
    public static Predicate<List> listNotBlank = (list) -> list != null && !list.isEmpty();
    public static Predicate<List> listIsBlank = (list) -> list == null || list.isEmpty();
    public static Predicate<Set> setNotBlank = (set) -> set != null && !set.isEmpty();
    public static Predicate<Set> setIsBlank = (set) -> set == null || set.isEmpty();
    public static BiPredicate<List, Integer> listMatchSize = (list, length) -> list != null && list.size() == length;
    public static BiPredicate<Map, String> mapContainKey = (map, key) -> map != null && map.containsKey(key);
    public static Predicate<Map> mapNotBlank = (map) -> map != null && !map.isEmpty();

    public static final Predicate<Object> notEmpty = (x) -> {
        if (x == null) {
            return false;
        }
        if (x instanceof Map) {
            return !((Map) x).isEmpty();
        }
        if (x instanceof List) {
            return !((List) x).isEmpty();
        }
        if (x instanceof Set) {
            return !((Set) x).isEmpty();
        }
        if (x instanceof String) {
            return StringUtils.isNotBlank((String) x);
        }
        if (x instanceof Object[]) {
            return ((Object[]) x).length > 0;
        }
        return true;
    };

    public static final boolean equals(String value, String... args) {
        if (value == null || arrayIsBlank.test(args)) {
            return false;
        }
        for (String arg : args) {
            if (StringUtils.equals(value, arg)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean equalsIgnoreCase(String value, String... args) {
        if (value == null || arrayIsBlank.test(args)) {
            return false;
        }
        for (String arg : args) {
            if (StringUtils.equalsIgnoreCase(value, arg)) {
                return true;
            }
        }
        return false;
    }


    public static <T> T getDefaultIfNull(T value, T defaultV) {
        return getDefaultIfNull(value, () -> defaultV);
    }

    public static <T> T getDefaultIfNull(T value, Supplier<T> tSupplier) {
        try {
            if (value == null) {
                return tSupplier.get();
            }
            if (value instanceof String && StringUtils.isBlank((String) value)) {
                return tSupplier.get();
            }
        } catch (Exception e) {
            ;
        }
        return value;
    }

    public static String toString(List targets) {
        if (listIsBlank.test(targets)) {
            return "";
        }
        return (String) targets.stream().filter(notEmpty).map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }

}
