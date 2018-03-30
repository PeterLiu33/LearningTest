package com.peterliu.lt.common.task.simple;

import com.google.common.collect.Sets;
import com.peterliu.lt.common.Asserts;
import com.peterliu.lt.common.task.Task;
import com.peterliu.lt.common.task.TaskListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.ClosedByInterruptException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liujun on 2018/1/9.
 */
@Slf4j
public abstract class DefaultTask implements Task {

    @Getter
    protected String name = this.getClass().getSimpleName() + System.currentTimeMillis();
    // 任务启动线程数, 默认一个
    @Getter
    protected volatile int threadSize = -1;
    // 结束标志位，true-结束
    @Getter
    protected volatile boolean finished = true;
    // 任务是否被终止
    @Setter
    @Getter
    protected volatile boolean jobInterrupted = false;
    // 针对每一个线程的状态
    protected volatile AtomicReferenceArray<Boolean> allFinished;
    // 任务超时时间，如果小于等于0，则无超时，超时会自动中断
    @Getter
    protected volatile long timeOut = -1;

    // 任务具体执行内容
    public Runnable runner;
    // 任务启动延时，默认没有延时
    protected int startDelay = 0;
    // 任务启动线程池
    private volatile AtomicReferenceArray<Thread> threads;
    // 任务启动线程池状态
    protected volatile AtomicReferenceArray<Thread.State> status;
    // 线程启动时间，单位毫秒
    protected volatile AtomicReferenceArray<Long> startTime;
    // 是否允许并发调用, 默认允许
    protected volatile boolean allowConcurrentRun = true;
    // 并发调用锁
    protected ReentrantLock lock = new ReentrantLock(true);

    // 监听器
    protected Set<TaskListener> taskListeners = Sets.newHashSet();

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);


    // 是否设置为守护线程
    @Getter
    private boolean daemon = false;


    @Override
    public Task assignMission(String name, Runnable runner) {
        Asserts.isNotBlank(name, "[%s]TaskNameIsEmpty!", name);
        setName(name);
        this.runner = runner;
        this.daemon = false;
        this.timeOut = -1;
        this.allowConcurrentRun = true;
        return this;
    }

    @Override
    public Task assignMission(String name, Runnable runner, boolean daemon) {
        assignMission(name, runner);
        this.daemon = daemon;
        return this;
    }

    @Override
    public Task assignMission(String name, Runnable runner, long timeOut) {
        assignMission(name, runner);
        this.timeOut = timeOut;
        return this;
    }

    @Override
    public Task assignMission(String name, boolean allowConcurrentRun, Runnable runner) {
        Asserts.isNotBlank(name, "[%s]TaskNameIsEmpty!", name);
        setName(name);
        this.runner = runner;
        this.daemon = false;
        this.timeOut = -1;
        this.allowConcurrentRun = allowConcurrentRun;
        return this;
    }

    @Override
    public Task assignMission(String name, boolean allowConcurrentRun, Runnable runner, boolean daemon) {
        assignMission(name, runner);
        this.daemon = daemon;
        this.allowConcurrentRun = allowConcurrentRun;
        return this;
    }

    @Override
    public Task assignMission(String name, boolean allowConcurrentRun, Runnable runner, long timeOut) {
        assignMission(name, runner);
        this.timeOut = timeOut;
        this.allowConcurrentRun = allowConcurrentRun;
        return this;
    }

    @Override
    public Thread.State getStatus(int index) {
        Asserts.isTrue(index < this.threadSize, "[%d]IndexShouldBe[0,%d]", index, this.threadSize);
        Asserts.isNotBlank(this.getThread(index), "[%d]CurrentThreadIsEmpty!", index);
        return this.getThread(index).getState();
    }

    @Override
    public Thread.State[] getStatus() {
        Thread.State temp[] = new Thread.State[this.threadSize];
        for (int i = 0; i < this.threadSize; i++) {
            temp[i] = this.status.get(i);
        }
        return temp;
    }

    @Override
    public void addListener(TaskListener listener) {
        this.taskListeners.add(listener);
    }

    //:内部方法/////////////////////////////////////////////////////////////////////////

    // 子类实现
    protected abstract void run(int index);

    /**
     * 启动线程
     */
    public void start() {
        if (!finished) {
            //线程还在运行，无法再次启动
            return;
        }
        this.clearStatus();
        // 设置启动状态
        this.finished = false;
        this.jobInterrupted = false;
        for (int i = 0; i < this.allFinished.length(); i++) {
            this.allFinished.set(i, false);
        }
        ThreadFactory threadFactory = new TaskThreadFactory(this);
        // 是否已经通知
        AtomicInteger notify = new AtomicInteger(0);
        for (int i = 0; i < threadSize; i++) {
            int finalI = i;
            Thread thread = threadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!DefaultTask.this.allFinished.get(finalI) && !jobInterrupted) {
                            // 意外中断可重启
                            try {
                                if (DefaultTask.this.startDelay > 0) {
                                    // 启动延时
                                    Thread.sleep(DefaultTask.this.startDelay);
                                }
                                // 设置启动时间，用于超时判断
                                DefaultTask.this.writeLock().lock();
                                try {
                                    DefaultTask.this.startTime.set(finalI, System.currentTimeMillis());
                                } finally {
                                    DefaultTask.this.writeLock().unlock();
                                }
                                // 执行真正的业务逻辑
                                if (notify.compareAndSet(0, 1)) {
                                    DefaultTask.this.notifyStart();
                                }
                                if (!DefaultTask.this.allowConcurrentRun) {
                                    // 不允许并发调用
                                    DefaultTask.this.lock.lock();
                                    try {
                                        DefaultTask.this.run(finalI);
                                    } finally {
                                        DefaultTask.this.lock.unlock();
                                    }
                                } else {
                                    // 允许并发调用
                                    DefaultTask.this.run(finalI);
                                }
                            } catch (Exception e) {
                                if (e instanceof InterruptedException ||
                                        e instanceof ClosedByInterruptException ||
                                        e instanceof SecurityException) {
                                    // 线程被强制中断，例如调用forceShutDown方法
                                    break;
                                } else {
                                    // 线程意外中断, 会恢复
                                    log.error(String.format("ThreadThrowException, thread-name:[%s]", Thread.currentThread().getName()), e);
                                }
                            }
                        }
                    } finally {
                        // 程序结束
                        DefaultTask.this.allFinished.set(finalI, true);
                        boolean temp = true;
                        for (int i1 = 0; i1 < DefaultTask.this.allFinished.length(); i1++) {
                            temp = temp && DefaultTask.this.allFinished.get(i1);
                        }
                        if (temp && notify.compareAndSet(1, 2)) {
                            // 所有程序都退出了
                            DefaultTask.this.clearStatus();
                            DefaultTask.this.notifyStop();
                        }
                    }
                }
            });
            thread.start();
            this.writeLock().lock();
            try {
                this.setThread(i, thread);
            } finally {
                this.writeLock().unlock();
            }
        }
    }

    /**
     * 强制中断线程
     *
     * @return true-中断成功，false-中断失败
     */
    public boolean forceShutDown() {
        boolean runStatus = true;
        this.readLock().lock();
        try {
            for (int i = 0; i < this.threadSize; i++) {
                Thread thread = this.getThread(i);
                if (thread != null) {
                    try {
                        // 设置中断标志位
                        this.jobInterrupted = true;
                        thread.interrupt();
                        log.info(String.format("ThreadHasForceShutDown, Thread-Name:[%s]", thread.getName()));
                    } catch (Exception e) {
                        // 有线程关闭失败
                        runStatus = false;
                        log.error(String.format("ThreadHasFailedToForceShutDown, Thread-Name:[%s]", thread.getName()), e);
                    }
                }
            }
        } finally {
            this.readLock().unlock();
        }
        return this.finished = runStatus;
    }

    /**
     * 设置结束标志位
     */
    public void end() {
        this.finished = true;
    }

    /**
     * 设置任务名称
     *
     * @param name
     */
    protected void setName(String name) {
        this.name = name + System.currentTimeMillis();
    }

    /**
     * 判断是否超时
     *
     * @return true-已经超时
     */
    public boolean checkTimeOut() {
        if (this.timeOut > 0) {
            //需要判断超时
            this.readLock().lock();
            try {
                for (int i = 0; i < this.threadSize; i++) {
                    if (this.getThread(i) != null && this.startTime.get(i) != null) {
                        if (System.currentTimeMillis() - this.startTime.get(i) > this.timeOut) {
                            // 超时
                            return true;
                        }
                    }
                }
            } finally {
                this.readLock().unlock();
            }
        }
        return false;
    }

    /**
     * 更新线程状态
     */
    public void updateStatus() {
        this.writeLock().lock();
        try {
            for (int i = 0; i < this.threadSize; i++) {
                if (this.getThread(i) != null) {
                    this.status.set(i, this.getThread(i).getState());
                }
            }
        } finally {
            this.writeLock().unlock();
        }
    }

    /**
     * 重新设置线程队列大小, 只有finished=true时才能生效
     *
     * @param threadSize
     * @return true-设置成功,false-设置失败
     */
    protected boolean resetConfig(int threadSize) {
        if (this.finished) {
            if (this.threadSize != threadSize) {
                this.writeLock().lock();
                try {
                    this.threadSize = threadSize;
                    this.allFinished = new AtomicReferenceArray<Boolean>(new Boolean[threadSize]);
                    for (int i = 0; i < this.allFinished.length(); i++) {
                        this.allFinished.set(i, true);
                    }
                    this.threads = new AtomicReferenceArray<Thread>(new Thread[threadSize]);
                    this.startTime = new AtomicReferenceArray<Long>(new Long[threadSize]);
                    this.status = new AtomicReferenceArray<Thread.State>(new Thread.State[threadSize]);
                } finally {
                    this.writeLock().unlock();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 清理状态
     */
    public void clearStatus() {
        this.writeLock().lock();
        try {
            this.finished = true;
            this.allFinished = new AtomicReferenceArray<Boolean>(new Boolean[threadSize]);
            for (int i = 0; i < this.allFinished.length(); i++) {
                this.allFinished.set(i, true);
            }
            this.threads = new AtomicReferenceArray<Thread>(new Thread[threadSize]);
            this.startTime = new AtomicReferenceArray<Long>(new Long[threadSize]);
            this.status = new AtomicReferenceArray<Thread.State>(new Thread.State[threadSize]);
        } finally {
            this.writeLock().unlock();
        }
    }

    /**
     * 获取线程状态
     *
     * @param index
     * @return
     */
    public Thread getThread(int index) {
        Asserts.isTrue(index >= 0 && index < this.threadSize, "[%d]indexIsIllegal!", index);
        readLock().lock();
        try {
            return this.threads.get(index);
        } finally {
            readLock().unlock();
        }
    }

    @Override
    public boolean isInterrupted() {
        return this.isJobInterrupted();
    }

    protected void notifyStart() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (TaskListener listener : DefaultTask.this.taskListeners) {
                    try {
                        listener.start(DefaultTask.this);
                    } catch (Exception e) {

                    }
                }
            }
        });
    }

    protected void notifyStop() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (TaskListener listener : DefaultTask.this.taskListeners) {
                    try {
                        listener.stop(DefaultTask.this);
                    } catch (Exception e) {

                    }
                }
            }
        });
    }

    private Long getStartTime(int index) {
        readLock().lock();
        try {
            return this.startTime.get(index);
        } finally {
            readLock().unlock();
        }
    }

    private void setThread(int index, Thread thread) {
        this.threads.set(index, thread);
    }

    // 读写锁
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Lock readLock() {
        return readWriteLock.readLock();
    }

    public Lock writeLock() {
        return readWriteLock.writeLock();
    }
}