package com.koflance.lt.java.lambda;

import com.koflance.lt.common.DateUtils;

/**
 * Created by liujun on 2018/4/18.
 */
public interface Lambda {

    String tr(String input);

    static void main(String[] args) {
        System.out.println(test("${yyyyMMdd|1d}", DateUtils::formatNow));
        System.out.println(test("${yyyyMMdd|1d}", (v) -> DateUtils.formatNow(v)));
    }

    static String test(String value, Lambda lambda){
        return lambda.tr(value);
    }
}
