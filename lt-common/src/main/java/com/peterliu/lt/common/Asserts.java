package com.peterliu.lt.common;

import org.apache.commons.lang.StringUtils;

/**
 * Created by liujun on 2018/1/26.
 */
public class Asserts {

    /**
     * 如果为空或空串、null则抛出异常
     *
     * @param obj       待检测对象，可以是string，或非string 对象
     * @param msgFormat 提示文案模板
     * @param params    提示文案模板参数
     * @throws IllegalArgumentException
     */
    public static void isNotBlank(Object obj, String msgFormat, Object... params) throws IllegalArgumentException {
        isNotBlank(obj, String.format(msgFormat, params));
    }

    /**
     * 如果为空或空串、null则抛出异常
     *
     * @param obj 待检测对象，可以是string，或非string 对象
     * @param msg 提示文案
     * @throws IllegalArgumentException
     */
    public static void isNotBlank(Object obj, String msg) throws IllegalArgumentException {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        } else if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                // 空串
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * 如果value为false/null，则抛出异常
     *
     * @param value     待检测对象
     * @param msgFormat 提示文案模板
     * @param params    提示文案模板参数
     * @throws IllegalArgumentException
     */
    public static void isTrue(Boolean value, String msgFormat, Object... params) throws IllegalArgumentException {
        isTrue(value, String.format(msgFormat, params));
    }

    /**
     * 如果value为false/null，则抛出异常
     *
     * @param value 待检测对象
     * @param msg   提示文案
     * @throws IllegalArgumentException
     */
    public static void isTrue(Boolean value, String msg) throws IllegalArgumentException {
        if (value == null || !value) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 如果value为true/null，则抛出异常
     *
     * @param value     待检测对象
     * @param msgFormat 提示文案模板
     * @param params    提示文案模板参数
     * @throws IllegalArgumentException
     */
    public static void isFalse(Boolean value, String msgFormat, Object... params) throws IllegalArgumentException {
        isFalse(value, String.format(msgFormat, params));
    }

    /**
     * 如果value为true/null，则抛出异常
     *
     * @param value 待检测对象
     * @param msg   提示文案
     * @throws IllegalArgumentException
     */
    public static void isFalse(Boolean value, String msg) throws IllegalArgumentException {
        if (value == null || value) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 如果left.equal(right) == False，则抛出异常
     *
     * @param left      待检测对象
     * @param right     待检测对象
     * @param msgFormat 提示文案模板
     * @param params    提示文案模板参数
     * @throws IllegalArgumentException
     */
    public static void isEqual(Object left, Object right, String msgFormat, Object... params) throws IllegalArgumentException {
        isEqual(left, right, String.format(msgFormat, params));
    }

    /**
     * 如果left.equal(right) == False，则抛出异常
     *
     * @param left  待检测对象
     * @param right 待检测对象
     * @param msg   提示文案
     * @throws IllegalArgumentException
     */
    public static void isEqual(Object left, Object right, String msg) throws IllegalArgumentException {
        if (left == null ? (right == null ? true : false) : left.equals(right)) {
            return;
        } else {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 如果left.equal(right) == True，则抛出异常
     *
     * @param left      待检测对象
     * @param right     待检测对象
     * @param msgFormat 提示文案模板
     * @param params    提示文案模板参数
     * @throws IllegalArgumentException
     */
    public static void isNotEqual(Object left, Object right, String msgFormat, Object... params) throws IllegalArgumentException {
        isNotEqual(left, right, String.format(msgFormat, params));
    }

    /**
     * 如果left.equal(right) == True，则抛出异常
     *
     * @param left  待检测对象
     * @param right 待检测对象
     * @param msg   提示文案
     * @throws IllegalArgumentException
     */
    public static void isNotEqual(Object left, Object right, String msg) throws IllegalArgumentException {
        if (left == null ? (right == null ? true : false) : left.equals(right)) {
            throw new IllegalArgumentException(msg);
        }
    }
}