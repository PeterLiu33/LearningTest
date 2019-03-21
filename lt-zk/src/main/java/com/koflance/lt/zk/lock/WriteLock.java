package com.koflance.lt.zk.lock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.zookeeper.CreateMode.EPHEMERAL_SEQUENTIAL;

/**
 * A <a href="package.html">protocol to implement an exclusive
 *  write lock or to elect a leader</a>. <p/> You invoke {@link #lock()} to
 *  start the process of grabbing the lock; you may get the lock then or it may be
 *  some time later. <p/> You can register a listener so that you are invoked
 *  when you get the lock; otherwise you can ask if you have the lock
 *  by calling {@link #isOwner()}
 *
 */
public class WriteLock extends ProtocolSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WriteLock.class);

    /*当前锁的根目录*/
    private final String dir;
    /*当前节点ID，也是零时节点的全路径，例如x-[sessionId]-0000000010，如果该id不为null，则视为零时节点创建好了*/
    private String id;
    /*当前节点对象*/
    private ZNodeName idName;
    private String ownerId;
    private String lastChildId;
    private byte[] data = {0x12, 0x34};
    private LockListener callback;
    private LockZooKeeperOperation zop;

    /**
     * zookeeper contructor for writelock
     * @param zookeeper zookeeper client instance
     * @param dir the parent path you want to use for locking
     * @param acl the acls that you want to use for all the paths,
     * if null world read/write is used.
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl) {
        super(zookeeper);
        this.dir = dir;
        if (acl != null) {
            setAcl(acl);
        }
        this.zop = new LockZooKeeperOperation();
    }

    /**
     * zookeeper contructor for writelock with callback
     * @param zookeeper the zookeeper client instance
     * @param dir the parent path you want to use for locking
     * @param acl the acls that you want to use for all the paths
     * @param callback the call back instance
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl,
                     LockListener callback) {
        this(zookeeper, dir, acl);
        this.callback = callback;
    }

    /**
     * return the current locklistener
     * @return the locklistener
     */
    public LockListener getLockListener() {
        return this.callback;
    }

    /**
     * register a different call back listener
     * @param callback the call back instance
     */
    public void setLockListener(LockListener callback) {
        this.callback = callback;
    }

    /**
     * Removes the lock or associated znode if
     * you no longer require the lock. this also
     * removes your request in the queue for locking
     * in case you do not already hold the lock.
     * @throws RuntimeException throws a runtime exception
     * if it cannot connect to zookeeper.
     */
    public synchronized void unlock() throws RuntimeException {

        if (!isClosed() && id != null) {
            // we don't need to retry this operation in the case of failure
            // as ZK will remove ephemeral files and we don't wanna hang
            // this process when closing if we cannot reconnect to ZK
            try {

                ZooKeeperOperation zopdel = new ZooKeeperOperation() {
                    public boolean execute() throws KeeperException,
                            InterruptedException {
                        zookeeper.delete(id, -1);
                        return Boolean.TRUE;
                    }
                };
                zopdel.execute();
            } catch (InterruptedException e) {
                LOG.warn("Caught: " + e, e);
                //set that we have been interrupted.
                Thread.currentThread().interrupt();
            } catch (KeeperException.NoNodeException e) {
                // do nothing
            } catch (KeeperException e) {
                LOG.warn("Caught: " + e, e);
                throw (RuntimeException) new RuntimeException(e.getMessage()).
                        initCause(e);
            }
            finally {
                if (callback != null) {
                    callback.lockReleased();
                }
                id = null;
            }
        }
    }

    /**
     * the watcher called on
     * getting watch while watching
     * my predecessor
     */
    private class LockWatcher implements Watcher {
        public void process(WatchedEvent event) {
            // lets either become the leader or watch the new/updated node
            LOG.debug("Watcher fired on path: " + event.getPath() + " state: " +
                    event.getState() + " type " + event.getType());
            try {
                lock();
            } catch (Exception e) {
                LOG.warn("Failed to acquire lock: " + e, e);
            }
        }
    }

    /**
     * a zoookeeper operation that is mainly responsible
     * for all the magic required for locking.
     */
    private  class LockZooKeeperOperation implements ZooKeeperOperation {

        /**
         * 判断当前是否已经创建过自己的节点，如果没有创建则创建
         * 主要用于针对创建节点时，server挂了，
         * client需要重连的场景，防止锁无法释放。
         *
         * @param prefix 节点前缀，格式为：x-[sessionId]
         * @param zookeeper zk客户端
         * @param dir 锁的根目录地址
         * @throws KeeperException
         * @throws InterruptedException
         * @return 返回包含了自己节点的子节点列表
         */
        private List<String> findPrefixInChildren(String prefix, ZooKeeper zookeeper, String dir)
                throws KeeperException, InterruptedException {
            /*获取所有的零时节点*/
            List<String> names = zookeeper.getChildren(dir, false);
            for (String name : names) {
                if (name.startsWith(prefix)) {
                    /*获取到id*/
                    id = name;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found id created last time: " + id);
                    }
                    break;
                }
            }
            if (id == null) {
                /*没有就创建*/
                id = zookeeper.create(dir + "/" + prefix, data,
                        getAcl(), EPHEMERAL_SEQUENTIAL);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created id: " + id);
                }
                /*重新获取*/
                names = zookeeper.getChildren(dir, false);
            }
            return names;
        }

        /**
         * 真正执行获取锁的动作，入口函数
         *
         * @return true-执行成功
         */
        public boolean execute() throws KeeperException, InterruptedException {
            do {
                List<String> names = null;
                if (id == null) {
                    /*如果异常原因造成重连，session只要没有过期，还是同一个*/
                    long sessionId = zookeeper.getSessionId();
                    String prefix = "x-" + sessionId + "-";
                    //判断是否已经创建过，没有则进行创建
                    names = findPrefixInChildren(prefix, zookeeper, dir);
                    idName = new ZNodeName(id);
                }
                if (id != null) {
                    if(names == null) {
                        names = zookeeper.getChildren(dir, false);
                    }
                    if (names.isEmpty()) {
                        LOG.warn("No children in: " + dir + " when we've just " +
                                "created one! Lets recreate it...");
                        // 告知节点不存在，必须下次重试需要重新创建
                        id = null;
                    } else {
                        // lets sort them explicitly (though they do seem to come back in order ususally :)
                        SortedSet<ZNodeName> sortedNames = new TreeSet<ZNodeName>();
                        for (String name : names) {
                            sortedNames.add(new ZNodeName(dir + "/" + name));
                        }
                        /*获取第一位*/
                        ownerId = sortedNames.first().getName();
                        /*获取比自己小的节点*/
                        SortedSet<ZNodeName> lessThanMe = sortedNames.headSet(idName);
                        if (!lessThanMe.isEmpty()) {
                            /*有比自己小的节点*/
                            /*获取比自己小一位的节点*/
                            ZNodeName lastChildName = lessThanMe.last();
                            lastChildId = lastChildName.getName();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("watching less than me node: " + lastChildId);
                            }
                            Stat stat = zookeeper.exists(lastChildId, new LockWatcher());
                            if (stat != null) {
                                return Boolean.FALSE;
                            } else {
                                LOG.warn("Could not find the" +
                                        " stats for less than me: " + lastChildName.getName());
                            }
                        } else {
                            /*没有比自己小的节点*/
                            if (isOwner()) {
                                if (callback != null) {
                                    callback.lockAcquired();
                                }
                                return Boolean.TRUE;
                            }
                        }
                    }
                }
            }
            while (id == null);
            return Boolean.FALSE;
        }
    };

    /**
     *
     * Attempts to acquire the exclusive write lock returning whether or not it was
     * acquired. Note that the exclusive lock may be acquired some time later after
     * this method has been invoked due to the current lock owner going away.
     */
    /**
     * 尝试去获得锁
     *
     * @return true-获得锁
     * @throws KeeperException
     * @throws InterruptedException
     */
    public synchronized boolean lock() throws KeeperException, InterruptedException {
        if (isClosed()) {
            return false;
        }
        ensurePathExists(dir);

        return (Boolean) retryOperation(zop);
    }

    /**
     * return the parent dir for lock
     * @return the parent dir used for locks.
     */
    public String getDir() {
        return dir;
    }

    /**
     * 判断是不是自己的节点
     */
    public boolean isOwner() {
        return id != null && ownerId != null && id.equals(ownerId);
    }

    /**
     * return the id for this lock
     * @return the id for this lock
     */
    public String getId() {
        return this.id;
    }

    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        ZooKeeper keeper = new ZooKeeper("localhost:2181,localhost:2182,localhost:2183", 3000000, null);
        WriteLock leader = new WriteLock(keeper, "/" + WriteLock.class.getName(), null);

        leader.lock();
    }

}
