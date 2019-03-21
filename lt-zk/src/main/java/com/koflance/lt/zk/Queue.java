package com.koflance.lt.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 这个简单案例，producer和consumer使用的是一个整数
 *
 * Created by liujun on 2017/11/22.
 */
public class Queue extends SyncPrimitive{

    /**
     * Constructor of producer-consumer queue
     *
     * @param address
     * @param name
     */
    Queue(String address, String name) {
        super(address);
        this.root = name;
        // Create ZK node name
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    /*创建znode节点*/
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
    }

    /**
     * Add element to the queue.
     *
     * @param i
     * @return
     */

    boolean produce(int i) throws KeeperException, InterruptedException{
        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] value;

        // Add child with value i
        b.putInt(i);
        value = b.array();
        /*创建持久有序节点*/
        zk.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

        return true;
    }

    /**
     * Remove first element from the queue.
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    int consume() throws KeeperException, InterruptedException{
        int retvalue = -1;
        Stat stat = null;
        String path = null;

        // Get the first element available
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                if (list.size() == 0) {
                    System.out.println("Going to wait");
                    /*没到，你睡吧*/
                    mutex.wait();
                } else {
                    Integer min = new Integer(list.get(0).substring(7));
                    for(String s : list){
                        /*去除掉element前缀，获取后面的序号*/
                        Integer tempValue = new Integer(s.substring(7));
                        //System.out.println("Temporary value: " + tempValue);
                        if(tempValue < min) min = tempValue;
                    }
                    path = String.format("/element%010d" ,min);
                    System.out.println("Temporary value: " + root + path);
                    byte[] b = zk.getData(root + path,
                            false, stat);
                    /*使用了zk的原子性，如果节点被其他消费了删除了，则这个操作将报KeeperException.NoNode*/
                    zk.delete(root + path, 0);
                    ByteBuffer buffer = ByteBuffer.wrap(b);
                    retvalue = buffer.getInt();

                    return retvalue;
                }
            }
        }
    }

    public static void main(String[] args) {
//        args = "localhost:2181,localhost:2182,localhost:2183 10 p".split(" ");
        args = "localhost:2181,localhost:2182,localhost:2183 10 c".split(" ");
        Queue q = new Queue(args[0], "/app1");

        System.out.println("Input: " + args[0]);
        int i;
        Integer max = new Integer(args[1]);

        if (args[2].equals("p")) {
            System.out.println("Producer");
            for (i = 0; i < max; i++)
                try{
                    q.produce(10 + i);
                } catch (KeeperException e){

                } catch (InterruptedException e){

                }
        } else {
            System.out.println("Consumer");

            for (i = 0; i < max; i++) {
                try{
                    int r = q.consume();
                    System.out.println("Item: " + r);
                } catch (KeeperException e){
                    i--;
                } catch (InterruptedException e){

                }
            }
        }
    }
}
