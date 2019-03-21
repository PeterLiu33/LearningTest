package com.koflance.lt.common;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by liujun on 11/08/2018.
 */
public abstract class LoggerFactory {

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
     * @param enableLog      是否需要打印日志
     * @return
     */
    public static Logger getLogger(String loggerName, Predicate<String> enableLog) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(loggerName);
        return (Logger.Level level, Supplier<String> logMsg, Optional<Throwable> eOpt) -> {
            try {
                if (enableLog.test(loggerName)) {
                    // 是否开启日志记录
                    switch (level) {
                        case info:
                            if (logger.isInfoEnabled()) {
                                if (eOpt.isPresent()) {
                                    logger.info(logMsg.get(), eOpt.get());
                                } else {
                                    logger.info(logMsg.get());
                                }
                            }
                            break;
                        case warn:
                            if (logger.isWarnEnabled()) {
                                if (eOpt.isPresent()) {
                                    logger.warn(logMsg.get(), eOpt.get());
                                } else {
                                    logger.warn(logMsg.get());
                                }
                            }
                            break;
                        case debug:
                            if (logger.isDebugEnabled()) {
                                if (eOpt.isPresent()) {
                                    logger.debug(logMsg.get(), eOpt.get());
                                } else {
                                    logger.debug(logMsg.get());
                                }
                            }
                            break;
                        case error:
                            if (logger.isErrorEnabled()) {
                                if (eOpt.isPresent()) {
                                    logger.error(logMsg.get(), eOpt.get());
                                } else {
                                    logger.error(logMsg.get());
                                }
                            }
                            break;
                        case trace:
                            if (logger.isTraceEnabled()) {
                                if (eOpt.isPresent()) {
                                    logger.trace(logMsg.get(), eOpt.get());
                                } else {
                                    logger.trace(logMsg.get());
                                }
                            }
                            break;
                        default:
                            return;
                    }
                }
            }catch (Exception e){
                // 日志记录失败
                logger.error("日志记录失败", e);
            }
        };
    }
}
