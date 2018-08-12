package com.peterliu.lt.common;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by liujun on 11/08/2018.
 */
@FunctionalInterface
public interface Logger {


    //info:--------------------------------------------------------

    default void info(Throwable e) {
        log(Level.info, e, () -> "");
    }

    default void info(String format, Supplier<Object>... args) {
        log(Level.info, format, args);
    }

    default void info(String format, Object... args) {
        log(Level.info, format, args);
    }

    default void info(Throwable e, String format, Supplier<Object>... args) {
        log(Level.info, e, format, args);
    }

    default void info(Throwable e, String format, Object... args) {
        log(Level.info, e, format, args);
    }

    default void info(Supplier<String> logMsg) {
        log(Level.info, logMsg);
    }

    default void info(Object msg) {
        log(Level.info, msg);
    }

    default void info(Supplier<String> logMsg, Throwable e) {
        log(Level.info, logMsg, e);
    }

    default void info(String msg, Throwable e) {
        log(Level.info, msg, e);
    }

    default void info(Supplier<String>... args) {
        log(Level.info, args);
    }

    default void info(String... args) {
        log(Level.info, args);
    }

    default void info(Throwable e, Supplier<String>... args) {
        log(Level.info, e, args);
    }

    default void info(Throwable e, String... args) {
        log(Level.info, e, args);
    }

    //warn:--------------------------------------------------------

    default void warn(Throwable e) {
        log(Level.warn, e, () -> "");
    }

    default void warn(String format, Supplier<Object>... args) {
        log(Level.warn, format, args);
    }

    default void warn(String format, Object... args) {
        log(Level.warn, format, args);
    }

    default void warn(Throwable e, String format, Supplier<Object>... args) {
        log(Level.warn, e, format, args);
    }

    default void warn(Throwable e, String format, Object... args) {
        log(Level.warn, e, format, args);
    }

    default void warn(Supplier<String> logMsg) {
        log(Level.warn, logMsg);
    }

    default void warn(Object msg) {
        log(Level.warn, msg);
    }

    default void warn(Supplier<String> logMsg, Throwable e) {
        log(Level.warn, logMsg, e);
    }

    default void warn(String msg, Throwable e) {
        log(Level.warn, msg, e);
    }

    default void warn(Supplier<String>... args) {
        log(Level.warn, args);
    }

    default void warn(String... args) {
        log(Level.warn, args);
    }

    default void warn(Throwable e, Supplier<String>... args) {
        log(Level.warn, e, args);
    }

    default void warn(Throwable e, String... args) {
        log(Level.warn, e, args);
    }

    //debug:--------------------------------------------------------

    default void debug(Throwable e) {
        log(Level.debug, e, () -> "");
    }

    default void debug(String format, Supplier<Object>... args) {
        log(Level.debug, format, args);
    }

    default void debug(String format, Object... args) {
        log(Level.debug, format, args);
    }

    default void debug(Throwable e, String format, Supplier<Object>... args) {
        log(Level.debug, e, format, args);
    }

    default void debug(Throwable e, String format, Object... args) {
        log(Level.debug, e, format, args);
    }

    default void debug(Supplier<String> logMsg) {
        log(Level.debug, logMsg);
    }

    default void debug(Object msg) {
        log(Level.debug, msg);
    }

    default void debug(Supplier<String> logMsg, Throwable e) {
        log(Level.debug, logMsg, e);
    }

    default void debug(String msg, Throwable e) {
        log(Level.debug, msg, e);
    }

    default void debug(Supplier<String>... args) {
        log(Level.debug, args);
    }

    default void debug(String... args) {
        log(Level.debug, args);
    }

    default void debug(Throwable e, Supplier<String>... args) {
        log(Level.debug, e, args);
    }

    default void debug(Throwable e, String... args) {
        log(Level.debug, e, args);
    }

    //error:--------------------------------------------------------

    default void error(Throwable e) {
        log(Level.error, e, () -> "");
    }

    default void error(String format, Supplier<Object>... args) {
        log(Level.error, format, args);
    }

    default void error(String format, Object... args) {
        log(Level.error, format, args);
    }

    default void error(Throwable e, String format, Supplier<Object>... args) {
        log(Level.error, e, format, args);
    }

    default void error(Throwable e, String format, Object... args) {
        log(Level.error, e, format, args);
    }

    default void error(Supplier<String> logMsg) {
        log(Level.error, logMsg);
    }

    default void error(Object msg) {
        log(Level.error, msg);
    }

    default void error(Supplier<String> logMsg, Throwable e) {
        log(Level.error, logMsg, e);
    }

    default void error(String msg, Throwable e) {
        log(Level.error, msg, e);
    }

    default void error(Supplier<String>... args) {
        log(Level.error, args);
    }

    default void error(String... args) {
        log(Level.error, args);
    }

    default void error(Throwable e, Supplier<String>... args) {
        log(Level.error, e, args);
    }

    default void error(Throwable e, String... args) {
        log(Level.error, e, args);
    }

    //trace:--------------------------------------------------------

    default void trace(Throwable e) {
        log(Level.trace, e, () -> "");
    }

    default void trace(String format, Supplier<Object>... args) {
        log(Level.trace, format, args);
    }

    default void trace(String format, Object... args) {
        log(Level.trace, format, args);
    }

    default void trace(Throwable e, String format, Supplier<Object>... args) {
        log(Level.trace, e, format, args);
    }

    default void trace(Throwable e, String format, Object... args) {
        log(Level.trace, e, format, args);
    }

    default void trace(Supplier<String> logMsg) {
        log(Level.trace, logMsg);
    }

    default void trace(Object msg) {
        log(Level.trace, msg);
    }

    default void trace(Supplier<String> logMsg, Throwable e) {
        log(Level.trace, logMsg, e);
    }

    default void trace(String msg, Throwable e) {
        log(Level.trace, msg, e);
    }

    default void trace(Supplier<String>... args) {
        log(Level.trace, args);
    }

    default void trace(String... args) {
        log(Level.trace, args);
    }

    default void trace(Throwable e, Supplier<String>... args) {
        log(Level.trace, e, args);
    }

    default void trace(Throwable e, String... args) {
        log(Level.trace, e, args);
    }

    //core:--------------------------------------------------------

    default void log(Level level, Throwable e) {
        log(level, e, () -> "");
    }

    default void log(Level level, String format, Supplier<Object>... args) {
        log(level, null, format, args);
    }

    default void log(Level level, String format, Object... args) {
        log(level, null, format, args);
    }

    default void log(Level level, Throwable e, String format, Supplier<Object>... args) {
        log(level, () -> String.format(format, args == null ? null : Arrays.stream(args).map(Supplier::get).toArray()), Optional.ofNullable(e));
    }

    default void log(Level level, Throwable e, String format, Object... args) {
        log(level, () -> String.format(format, args), Optional.ofNullable(e));
    }

    default void log(Level level, Supplier<String> logMsg) {
        log(level, logMsg, Optional.empty());
    }

    default void log(Level level, Object msg) {
        log(level, () -> String.valueOf(msg), Optional.empty());
    }

    default void log(Level level, Supplier<String> logMsg, Throwable e) {
        log(level, logMsg, Optional.ofNullable(e));
    }

    default void log(Level level, String msg, Throwable e) {
        log(level, () -> msg, Optional.ofNullable(e));
    }

    default void log(Level level, Supplier<String>... args) {
        Throwable e = null;
        log(level, e, args);
    }

    default void log(Level level, String... args) {
        Throwable e = null;
        log(level, e, args);
    }

    default void log(Level level, Throwable e, Supplier<String>... args) {
        log(level, () -> StringUtils.join(args == null ? null : Arrays.stream(args).map(Supplier::get).toArray(), ","), Optional.ofNullable(e));
    }

    default void log(Level level, Throwable e, String... args) {
        log(level, () -> StringUtils.join(args, ","), Optional.ofNullable(e));
    }

    /**
     * 需要子类实现
     *
     * @param level  info, warn
     * @param logMsg 日志消息
     * @param eOpt
     */
    void log(Level level, Supplier<String> logMsg, Optional<Throwable> eOpt);

    enum Level {
        info, warn, debug, error, trace
    }
}
