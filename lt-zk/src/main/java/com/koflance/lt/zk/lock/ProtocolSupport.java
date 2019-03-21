package com.koflance.lt.zk.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A base class for protocol implementations which provides a number of higher
 * level helper methods for working with ZooKeeper along with retrying synchronous
 * operations if the connection to ZooKeeper closes such as
 * {@link #retryOperation(ZooKeeperOperation)}
 */
class ProtocolSupport {
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolSupport.class);

    /*zk客户端*/
    protected final ZooKeeper zookeeper;
    private AtomicBoolean closed = new AtomicBoolean(false);
    /*重试延时，单位毫秒*/
    private long retryDelay = 500L;
    /*重试次数，比如zk链接中断后*/
    private int retryCount = 10;
    private List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    public ProtocolSupport(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    /**
     * Closes this strategy and releases any ZooKeeper resources; but keeps the
     * ZooKeeper instance open
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    /**
     * return zookeeper client instance
     *
     * @return zookeeper client instance
     */
    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    /**
     * return the acl its using
     *
     * @return the acl.
     */
    public List<ACL> getAcl() {
        return acl;
    }

    /**
     * set the acl
     *
     * @param acl the acl to set to
     */
    public void setAcl(List<ACL> acl) {
        this.acl = acl;
    }

    /**
     * get the retry delay in milliseconds
     *
     * @return the retry delay
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the time waited between retry delays
     *
     * @param retryDelay the retry delay
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Allow derived classes to perform
     * some custom closing operations to release resources
     */
    protected void doClose() {
    }


    /**
     * 用于重试代理，针对于session超时或连接中断重试情况
     * session超时，直接抛出SessionExpiredException异常，并不重试
     *
     * @return 返回对象，需要自己转类型.
     */
    protected Object retryOperation(ZooKeeperOperation operation)
            throws KeeperException, InterruptedException {
        KeeperException exception = null;
        for (int i = 0; i < retryCount; i++) {
            try {
                return operation.execute();
            } catch (KeeperException.SessionExpiredException e) {
                /*session超时，不重试*/
                LOG.warn("Session expired for: " + zookeeper + " so reconnecting due to: " + e, e);
                throw e;
            } catch (KeeperException.ConnectionLossException e) {
                if (exception == null) {
                    exception = e;
                }
                LOG.debug("Attempt " + i + " failed with connection loss so " +
                        "attempting to reconnect: " + e, e);
                /*连接丢失，重试延时*/
                retryDelay(i);
            }
        }
        throw exception;
    }

    /**
     * 确保锁的根目录存在，注意这里用的是znode节点，是持久化的
     *
     * @param path
     */
    protected void ensurePathExists(String path) {
        ensureExists(path, null, acl, CreateMode.PERSISTENT);
    }

    /**
     * 确保指定path已经存在，如果不存在，则创建该节点
     *
     * @param path
     * @param acl
     * @param flags
     */
    protected void ensureExists(final String path, final byte[] data,
                                final List<ACL> acl, final CreateMode flags) {
        try {
            retryOperation(new ZooKeeperOperation() {
                public boolean execute() throws KeeperException, InterruptedException {
                    Stat stat = zookeeper.exists(path, false);
                    if (stat != null) {
                        return true;
                    }
                    zookeeper.create(path, data, acl, flags);
                    return true;
                }
            });
        } catch (KeeperException e) {
            LOG.warn("Caught: " + e, e);
        } catch (InterruptedException e) {
            LOG.warn("Caught: " + e, e);
        }
    }

    /**
     * Returns true if this protocol has been closed
     *
     * @return true if this protocol is closed
     */
    protected boolean isClosed() {
        return closed.get();
    }

    /**
     * 用于重试延时，随着重试次数越多，延时越多
     *
     * @param attemptCount 重试次数
     */
    protected void retryDelay(int attemptCount) {
        if (attemptCount > 0) {
            try {
                Thread.sleep(attemptCount * retryDelay);
            } catch (InterruptedException e) {
                LOG.debug("Failed to sleep: " + e, e);
            }
        }
    }
}
