package com.peterliu.lt.java.condition;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 类似与LinkedBlockingQueue，不同之处在于：
 * 1. 不支持迭代，以及少了部分方法，比如add、remove等
 * 2. 存储不是链表结构，而是参考了ArrayBlockingQueue，使用了数组，用于节省空间
 * <p>
 * 该类可以认为是ArrayBlockingQueue和LinkedBlockingQueue的结合，即支持并行读写，也使用array缩减队列空间花费。
 * <p>
 * Created by liujun on 2018/2/1.
 */
public class ConcurrentArrayBlockingQueue<E> implements BlockingQueue<E> {

    // 当前存储数据量
    private final AtomicInteger count;
    // 当前写入数据的index，从0开始
    private int offerPos;
    // 当前读出数据的index，从0开始
    private int pollPos;
    // 读互斥锁
    private final ReentrantLock readLock;
    // 非空，请读取
    private final Condition notEmptyPlsRead;
    // 写互斥锁
    private final ReentrantLock writeLock;
    // 非满，请写入
    private final Condition notFullPlsWrite;


    // 缓存
    private final Object items[];

    /**
     * @param capacity 队列大小
     * @param fair     true-公平锁
     */
    public ConcurrentArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = new Object[capacity];
        this.count = new AtomicInteger(0);
        this.readLock = new ReentrantLock(fair);
        this.notEmptyPlsRead = this.readLock.newCondition();
        this.writeLock = new ReentrantLock(fair);
        this.notFullPlsWrite = this.writeLock.newCondition();

    }

    @Override
    public boolean offer(E e) {
        // 判断元素是否为null
        checkNotNull(e);
        final AtomicInteger count = this.count;
        if (count.get() == items.length) {
            // 队列已经满
            return false;
        }
        int c = -1;
        final Lock lock = this.writeLock;
        lock.lock();
        try {
            if (count.get() < items.length) {
                // 可以插入
                enQueue(e);
                // 获取插入之前个数，并个数加1
                c = this.count.getAndIncrement();
                if (c + 1 < items.length) {
                    // 没有满，请继续写
                    notFullPlsWrite.signal();
                }
            }
        } finally {
            lock.unlock();
        }
        if (c == 0) {
            // 临界：说明之前读线程有可能在等待
            signalNotEmpty();
        }
        return c >= 0;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        // 判断元素是否为null
        checkNotNull(e);
        final AtomicInteger count = this.count;
        long nanoTime = unit.toNanos(timeout);
        int c = -1;
        final Lock lock = this.writeLock;
        lock.lockInterruptibly();
        try {
            while (count.get() == items.length) {
                if (nanoTime <= 0) {
                    // 等待时间到
                    return false;
                }
                // 等待唤醒
                nanoTime = notFullPlsWrite.awaitNanos(nanoTime);
            }
            // 可以插入
            enQueue(e);
            // 获取插入之前个数，并个数加1
            c = count.getAndIncrement();
            if (c + 1 < items.length) {
                // 没有满，请继续写
                notFullPlsWrite.signal();
            }
        } finally {
            lock.unlock();
        }
        if (c == 0) {
            // 临界：说明之前读线程有可能在等待
            signalNotEmpty();
        }
        return false;
    }

    @Override
    public void put(E e) throws InterruptedException {
        // 判断元素是否为null
        checkNotNull(e);
        final AtomicInteger count = this.count;
        int c = -1;
        final Lock lock = this.writeLock;
        lock.lockInterruptibly();
        try {
            while (count.get() == items.length) {
                // 一直等待
                notFullPlsWrite.await();
            }
            // 可以插入
            enQueue(e);
            // 获取插入之前个数，并个数加1
            c = count.getAndIncrement();
            if (c + 1 < items.length) {
                // 没有满，请继续写
                notFullPlsWrite.signal();
            }
        } finally {
            lock.unlock();
        }
        if (c == 0) {
            // 临界：说明之前读线程有可能在等待
            signalNotEmpty();
        }
    }

    @Override
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            // 队列空
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.readLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                // 可以删除
                x = deQueue();
                // 获取删除之前个数，并个数减1
                c = count.getAndDecrement();
                if (c > 1) {
                    // 没有空，请继续读
                    notEmptyPlsRead.signal();
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c == items.length) {
            // 临界：说明之前写线程有可能在等待
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.readLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0)
                    // 等待时间到
                    return null;
                // 等待唤醒
                nanos = notEmptyPlsRead.awaitNanos(nanos);
            }
            // 可以删除
            x = deQueue();
            // 获取删除之前个数，并个数减1
            c = count.getAndDecrement();
            if (c > 1) {
                // 没有空，请继续读
                notEmptyPlsRead.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == items.length) {
            // 临界：说明之前写线程有可能在等待
            signalNotFull();
        }
        return x;
    }

    @Override
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.readLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                // 一直等待下去
                notEmptyPlsRead.await();
            }
            // 可以删除
            x = deQueue();
            // 获取删除之前个数，并个数减1
            c = count.getAndDecrement();
            if (c > 1) {
                // 没有空，请继续读
                notEmptyPlsRead.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == items.length) {
            // 临界：说明之前写线程有可能在等待
            signalNotFull();
        }
        return x;
    }

    @Override
    public E peek() {
        if (count.get() == 0)
            return null;
        final ReentrantLock takeLock = this.readLock;
        takeLock.lock();
        try {
            return (E) this.items[pollPos];
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public int size() {
        return this.count.get();
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
     * 发送非满信号
     *
     * @param e
     * @return 当前队列个数
     */
    private void enQueue(E e) {
        final Object[] items = this.items;
        items[offerPos] = e;
        if (++offerPos == items.length) {
            offerPos = 0;
        }
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
        return item;
    }

    /**
     * 队列非空啦，请继续读
     */
    private void signalNotEmpty() {
        final Lock lock = this.readLock;
        lock.lock();
        try {
            notEmptyPlsRead.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 队列非满啦，请继续写
     */
    private void signalNotFull() {
        final Lock lock = this.writeLock;
        lock.lock();
        try {
            notFullPlsWrite.signal();
        } finally {
            lock.unlock();
        }
    }
}
