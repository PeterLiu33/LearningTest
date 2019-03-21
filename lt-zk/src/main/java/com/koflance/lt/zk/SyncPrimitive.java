package com.koflance.lt.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by liujun on 2017/11/22.
 */
public class SyncPrimitive implements Watcher{

    static ZooKeeper zk = null;
    /*互斥锁*/
    static Integer mutex;

    String root;

    public SyncPrimitive(String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 3000, this);
                mutex = new Integer(-1);
                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
    }

    @Override
    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            /*有变化，你醒来看下*/
            mutex.notify();
        }
    }
}
