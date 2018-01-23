package com.peterliu.lt.zk.lock;

import org.apache.zookeeper.KeeperException;

/**
 * 配合ProtocolSupport.retryOperation使用，用于重试链接丢失
 * <p>
 * {@link ProtocolSupport} class
 */
public interface ZooKeeperOperation {

    /**
     * 具体操作
     *
     * @return true-成功
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean execute() throws KeeperException, InterruptedException;
}
