package com.peterliu.lt.java.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * MCS队列锁（自旋锁，单向链表next，自旋在自己节点，设置next节点false）
 *
 * 缺点：
 * 优点是适用于NUMA系统架构，缺点是释放锁也需要自旋等待，且比CLH读、写、CAS等操作调用次数多。
 * Created by liujun on 2018/3/6.
 */
public class MCSLock implements Lock {

    private AtomicReference<QNode> tail;
    ThreadLocal<QNode> current;

    public MCSLock(){
        // 初始化为null
        tail = new AtomicReference<QNode>(null);
        current = new ThreadLocal<QNode>(){
            @Override
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    @Override
    public void lock() {
        // 获取当前节点
        QNode lastOne = current.get();
        QNode preOne = tail.getAndSet(lastOne);
        if(preOne != null){
            // 说明当前并不是最后一个节点, 需要自旋
            lastOne.locked = true;
            // 加入队列
            preOne.next = lastOne;
            // 自旋
            while (lastOne.locked){
                // 自旋转
            }
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
        QNode curr = current.get();
        if(curr.next == null){
            // 无后继节点
            if(tail.compareAndSet(curr, null)){
                // 清空tail
                return;
            }
            // 设置失败，说明有新节点加入，等待新节点设置next
            while (curr.next == null) {
            }
        }
        // 唤醒后续节点
        curr.next.locked = false;
        curr.next = null;
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    /**
     * MCS队列节点
     */
    private static class QNode{
        /*true表示该线程须要获取锁，且不释放锁；为false表示线程释放了锁*/
        private volatile boolean locked = false;
        private volatile QNode next;
    }
}
