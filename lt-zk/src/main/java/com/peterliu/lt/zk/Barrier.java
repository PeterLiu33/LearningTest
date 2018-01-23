package com.peterliu.lt.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

/**
 * Created by liujun on 2017/11/22.
 */
public class Barrier extends SyncPrimitive {

    private int size;
    private String name;

    /**
     * 创建栅栏根节点，后续所有的处理应用，都是在这个节点下创建子节点
     *
     * @param address
     * @param root
     * @param size
     */
    Barrier(String address, String root, int size) {
        super(address);
        this.root = root;
        this.size = size;

        // Create barrier node
        if (zk != null) {
            try {
                /*同步调用，判断节点是否存在*/
                Stat s = zk.exists(root, false);
                if (s == null) {
                    /*创建一个znode节点*/
                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                System.out
                        .println("Keeper exception when instantiating queue: "
                                + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }

        // My node name
        try {
            name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
        } catch (UnknownHostException e) {
            System.out.println(e.toString());
        }

    }

    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean enter() throws KeeperException, InterruptedException{
        /*创建临时有序节点*/
        zk.create(root + "/" + name, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);

                if (list.size() < size) {
                    /*没到，你睡吧*/
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Wait until all reach barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean leave() throws KeeperException, InterruptedException{
        /*删除临时有序节点*/
        zk.delete(root + "/" + name, 0);
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                if (list.size() > 0) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    public static void main(String[] args) {
        args = "localhost:2181,localhost:2182,localhost:2183 2".split(" ");
        Barrier b = new Barrier(args[0], "/b1", new Integer(args[1]));
        try{
            boolean flag = b.enter();
            System.out.println("Entered barrier: " + args[1]);
            if(!flag) System.out.println("Error when entering the barrier");
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }

        // Generate random integer
        Random rand = new Random();
        int r = rand.nextInt(100);
        // Loop for rand iterations
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        try{
            b.leave();
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }
        System.out.println("Left barrier");
    }
}
