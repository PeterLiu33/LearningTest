package com.peterliu.lt.java.condition;

import java.util.concurrent.TimeUnit;

/**
 * Created by liujun on 2018/2/1.
 */
public interface BlockingQueue<E> {

    /**
     * 向队列尾部插入元素, 不会等待会立即返回
     *
     * @param e 不允许为null
     * @return true-插入成功，false-插入失败
     */
    boolean offer(E e);

    /**
     * 向队列尾部插入元素, 会等待指定时间
     *
     * @param e       不允许为null
     * @param timeout
     * @param unit
     * @return true-插入成功，false-插入失败
     */
    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 向队列尾部插入元素, 会一直等待，直到可写
     *
     * @param e 不允许为null
     * @throws InterruptedException
     */
    void put(E e) throws InterruptedException;

    /**
     * 从队列头中获取元素，并从队列中删除该元素, 不会等待会立即返回
     *
     * @return null-如果没有值
     */
    E poll();

    /**
     * 从队列头中获取元素，并从队列中删除该元素, 会等待指定时间
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 从队列头中获取元素，并从队列中删除该元素, 会一直等待，直到可读
     *
     * @return
     * @throws InterruptedException
     */
    E take() throws InterruptedException;

    /**
     * 从队列头中获取元素，但并不从队列中删除元素
     *
     * @return
     */
    E peek();

    /**
     * 返回当前队列大小
     *
     * @return
     */
    int size();
}
