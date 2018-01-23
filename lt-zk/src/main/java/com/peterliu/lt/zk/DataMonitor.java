package com.peterliu.lt.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

import static org.apache.zookeeper.KeeperException.Code;

/**
 * 采用异步回调方式（zk.exists）判断获取节点的数据，并进行判断是否和之前的一致，
 * 不一致说明变更过，会触发listener.exist，
 * 如果发现节点不存在了或者节点无权限或sessiontimeout，则触发listener.close
 *
 * Created by liujun on 2017/11/20.
 */
public class DataMonitor implements Watcher, AsyncCallback.StatCallback {

    private ZooKeeper zk;
    private String znode;
    private DataMonitorListener listener;
    private Watcher chainedWatcher = null;
    public boolean dead;
    private byte[] prevData;

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // 异步回调方式，判断节点是否存在，如果存在则获取数据，
        // 如果对比之前数据发现不一致，则触发listener
        zk.exists(znode, true, this, null);
    }

    /**
     * 判断节点状态的异步回调函数
     *
     * @param rc 返回节点状态
     * @param path 节点路径
     * @param ctx 上下文，即zk.exists传入的ctx参数
     * @param stat 节点当前状态元数据
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (Code.get(rc)) {
            case OK:
                exists = true;
                break;
            case NONODE:
                exists = false;
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                dead = true;
                /*没有权限访问或者session过期，即没有ACL权限，直接关闭*/
                listener.closing(rc);
                return;
            default:
                // 重新监听，直至有效，这样就迭代循环了
                // 异步回调方式，判断节点是否存在，如果存在则获取数据，
                // 如果对比之前数据发现不一致，则触发listener
                zk.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                /*
                * 如果存在, 则获取指定znode路径的数据，
                * 如果watch=true，则会在获取数据的同时，放一个哨兵到znode，下次有变更，则会触发
                */
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            /*如果数据和上次不一样，改变了*/
            listener.exists(b);
            prevData = b;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        /*判断时间类型*/
        if (event.getType() == Event.EventType.None) {
            // 链接状态发生改变
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // 链接失效了
                    dead = true;
                    listener.closing(Code.SESSIONEXPIRED.intValue());
                    break;
            }
        } else {
            if (path != null && path.equals(znode)) {
                // 异步回调方式，判断节点是否存在，如果存在则获取数据，
                // 如果对比之前数据发现不一致，则触发listener
                zk.exists(znode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }

    public interface DataMonitorListener {
        /**
         * 节点数据发生变更
         */
        void exists(byte data[]);

        /**
         * zookeeper的会话过期或者没有节点访问权限
         *
         * @param rc the ZooKeeper reason code
         */
        void closing(int rc);
    }
}
