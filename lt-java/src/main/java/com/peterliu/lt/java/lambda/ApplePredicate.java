package com.peterliu.lt.java.lambda;

import org.apache.commons.lang.StringUtils;

/**
 * Created by liujun on 2018/4/18.
 */
public interface ApplePredicate {

    static boolean isGreen(Apple apple){
        return StringUtils.equals("green", apple.getColor());
    }
}
