package com.peterliu.lt.common;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by liujun on 12/08/2018.
 */
public abstract class LocalLoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz, (name) -> true);
    }

    public static Logger getLogger(String loggerName) {
        return getLogger(loggerName, (name) -> true);
    }

    /**
     * @param clazz
     * @param enableLog 是否需要打印日志
     * @return
     */
    public static Logger getLogger(Class<?> clazz, Predicate<String> enableLog) {
        return getLogger(clazz.getName(), enableLog);
    }

    /**
     * @param loggerName
     * @param enableLog  是否需要打印日志
     * @return
     */
    public static Logger getLogger(String loggerName, Predicate<String> enableLog) {
        return (Logger.Level level, Supplier<String> logMsg, Optional<Throwable> eOpt) -> {
            if (enableLog.test(loggerName)) {
                // 是否开启日志记录
                PrintStream printStream = null;
                switch (level) {
                    case info:
                    case trace:
                        printStream = System.out;
                        break;
                    case warn:
                    case debug:
                    case error:
                        printStream = System.err;
                        break;
                    default:
                        return;

                }
                if (eOpt.isPresent()) {
                    String msg = String.format("%s %s [%s] %s\n%s: %s\n\u0009%s",
                            DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS),
                            loggerName,
                            level.name().toUpperCase(),
                            logMsg.get(),
                            eOpt.get().getClass().getName(),
                            eOpt.get().getMessage(),
                            StringUtils.join(eOpt.get().getStackTrace(), "\n\u0009"));
                    printStream.println(msg);
                } else {
                    String msg = String.format("%s %s [%s] %s",
                            DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS),
                            loggerName,
                            level.name().toUpperCase(),
                            logMsg.get());
                    printStream.println(msg);
                }
            }
        };
    }
}
