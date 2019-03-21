package com.koflance.lt.java.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * CLH自旋锁，单项链表，自旋本地前驱节点QNode变量locked
 *
 * 缺点：
 *
 *  在NUMA系统结构下性能非常差。在这样的系统结构下，每一个线程有自己的内存，
 *  假设前趋结点的内存位置比較远。自旋推断前趋结点的locked域，性能将大打折扣，
 *  可是在SMP系统结构下该法还是非常有效的。
 *
 * Created by liujun on 2018/3/6.
 */
public class CLHLock implements Lock {

    private AtomicReference<QNode> tail;
    private ThreadLocal<QNode> pre;
    private ThreadLocal<QNode> current;

    public CLHLock(){
        // "初始节点"：会作为第一个节点的前趋节点，其lock为false，默认表示已经释放
        tail = new AtomicReference<>(new QNode());
        current = new ThreadLocal<QNode>(){
            @Override
            protected QNode initialValue() {
                // 每次获取锁时，需要创建一个新的节点
                return new QNode();
            }
        };
        pre = new ThreadLocal<QNode>(){
            @Override
            protected QNode initialValue() {
                // 每次获取锁时，前驱节点需要设置，而不是创建
                return null;
            }
        };
    }

    @Override
    public void lock() {
        // 创建新节点
        QNode lastOne = current.get();
        // 设置新节点的locked为true
        lastOne.locked = true;
        // 获取前驱节点
        QNode preOne = tail.getAndSet(lastOne);
        while (preOne.locked){
            // 自旋等待前驱节点释放锁
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
        // 获取当前节点
        QNode currentNode = current.get();
        // 设置新节点的locked为false
        currentNode.locked = false;
        // 把前驱节点设置为最开始的"初始节点"
        current.set(pre.get());
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    /**
     * CLH队列节点
     */
    private static class QNode{
        /*true表示该线程须要获取锁，且不释放锁；为false表示线程释放了锁*/
        private volatile boolean locked = false;
    }
}
