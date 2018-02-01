package com.peterliu.lt.common;

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
}
