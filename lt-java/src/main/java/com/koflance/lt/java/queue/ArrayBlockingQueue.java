package com.koflance.lt.java.queue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞队列，同一时间只有一个线程读写队列，这里没有迭代器itr参数哦
 * 多线程读/写同一个缓冲区：
 * 当向缓冲区中写入数据之后，唤醒"读线程"；
 * 当从缓冲区读出数据之后，唤醒"写线程"；
 * 并且当缓冲区满的时候，"写线程"需要等待；
 * 当缓冲区为空时，"读线程"需要等待。
 * <p>
 * Created by liujun on 2018/2/1.
 */
public class ArrayBlockingQueue<E> implements BlockingQueue<E>{
    // 互斥锁
    private final Lock lock;
    // 非空，请读取
    private final Condition notEmptyPlsRead;
    // 非满，请写入
    private final Condition notFullPlsWrite;
    // 当前存储数据量
    private volatile int count;
    // 当前写入数据的index，从0开始
    private int offerPos;
    // 当前读出数据的index，从0开始
    private int pollPos;

    // 缓存
    private final Object items[];

    /**
     * @param capacity 队列大小
     * @param fair     true-公平锁
     */
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notEmptyPlsRead = lock.newCondition();
        this.notFullPlsWrite = lock.newCondition();
    }

    @Override
    public boolean offer(E e) {
        // 判断元素是否为null
        checkNotNull(e);
        final Lock lock = this.lock;
        lock.lock();
        try {
            if (this.count == items.length) {
                // 队列已经满
                return false;
            } else {
                // 写入队列
                enQueue(e);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        // 判断元素是否为null
        checkNotNull(e);
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        // 获取等待时间
        long nanoTime = unit.toNanos(timeout);
        try {
            while (this.count == items.length) {
                if (nanoTime <= 0) {
                    // 等待时间到
                    return false;
                }
                // 写入剩余等待时间
                nanoTime = notFullPlsWrite.awaitNanos(nanoTime);
            }
            // 可以写入
            enQueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        // 判断元素是否为null
        checkNotNull(e);
        final Lock lock = this.lock;
        lock.lock();
        try {
            while (this.count == items.length) {
                notFullPlsWrite.await();
            }
            // 写入队列
            enQueue(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        final Lock lock = this.lock;
        lock.lock();
        try {
            if (this.count == 0) {
                return null;
            }
            return deQueue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        final Lock lock = this.lock;
        long nanoTime = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (this.count == 0){
                if(nanoTime <= 0){
                    return null;
                }
                nanoTime = notEmptyPlsRead.awaitNanos(nanoTime);
            }
            return deQueue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        try{
            while (this.count == 0){
                // 一直等待
                notEmptyPlsRead.await();
            }
            return deQueue();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public E peek() {
        final Lock lock = this.lock;
        lock.lock();
        try{
            return (E) this.items[pollPos];
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 判断是否为空
     *
     * @param v
     */
    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    /**
     * 增加count、offerPos
     * 发送非空读信号
     *
     * @param e
     */
    private void enQueue(E e) {
        final Object[] items = this.items;
        items[offerPos] = e;
        if (++offerPos == items.length) {
            offerPos = 0;
        }
        count++;
        notEmptyPlsRead.signal();
    }

    /**
     * 增加pollPos
     * 减少count
     * 发送非满写信号
     *
     * @return
     */
    private E deQueue() {
        final Object[] items = this.items;
        E item = (E) items[pollPos];
        // 置空，这是一个优化点
        items[pollPos] = null;
        if (++pollPos == items.length) {
            pollPos = 0;
        }
        count--;
        //没有迭代器
        notFullPlsWrite.signal();
        return item;
    }
}
