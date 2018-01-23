package com.peterliu.lt.zk;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 采用ZK实现的，分布式的场景下的，可重入读写锁
 * 持有写锁的时候，可以直接获得读锁，但反过来不行
 * 默认是非公平锁
 *
 * Created by liujun on 2017/11/24.
 */
public class ZkDistributedReentrantReadWriteLock implements ReadWriteLock , java.io.Serializable{

    /*读锁*/
    private final ReadLock readLock;
    /*写锁*/
    private final WriteLock writeLock;
    /*默认非公平锁*/
    private final boolean fair;

    public ZkDistributedReentrantReadWriteLock() {
        this(false);
    }

    public ZkDistributedReentrantReadWriteLock(boolean fair) {
        this.fair = fair;
        readLock = new ReadLock(this);
        writeLock = new WriteLock(this);
    }


    @Override
    public Lock readLock() {
        return null;
    }

    @Override
    public Lock writeLock() {
        return null;
    }

    public boolean isFair(){
        return this.fair;
    }

    public static class ReadLock implements Lock, java.io.Serializable{

        private final boolean fair;

        public ReadLock(ZkDistributedReentrantReadWriteLock lock){
            this.fair = lock.isFair();
        }

        @Override
        public void lock() {

        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    public static class WriteLock implements Lock, java.io.Serializable{

        private final boolean fair;

        public WriteLock(ZkDistributedReentrantReadWriteLock lock){
            this.fair = lock.isFair();
        }

        @Override
        public void lock() {

        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }
}
