package com.peterliu.lt.zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;

/**
 * 在指定的znode路径节点上，使用DataMonitor获取节点数据或状态变更情况。
 * 这个类会观察指定znode节点并保存数据在该路径上，当znode存在时，启动指定的程序；当znode节点不存在时，关闭指定的程序。
 *
 * Created by liujun on 2017/11/20.
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {
    private final DataMonitor dm;
    private final ZooKeeper zk;
    private final String filename;
    private final String[] exec;
    /*需要执行命令的程序*/
    private Process child;

    public static void main(String[] args) {
        args = "localhost:2181,localhost:2182,localhost:2183 /test /Users/liujun/Documents/test/zookeeper/executor.txt cat /Users/liujun/Documents/test/zookeeper/executor.txt".split(" ");
//        if (args.length < 4) {
//            System.err
//                    .println("USAGE: Executor hostPort znode filename program [args ...]");
//            System.exit(2);
//        }
        String hostPort = args[0];
        String znode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, znode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param hostPort 集群的host地址列表，多个用逗号分隔，例如host:port,host:port,host:port/app/a
     * @param znode  访问的节点路径
     * @param filename
     * @param exec 执行的命令程序，例如create xxx
     * @throws KeeperException
     * @throws IOException
     */
    public Executor(String hostPort, String znode, String filename,
                    String exec[]) throws KeeperException, IOException {
        this.filename = filename;
        this.exec = exec;
        /*
        * 创建ZK客户端，但要注意，该对象实例化时，并不会进行连接服务端，而只是初始化连接，真正的连接是异步。
        * zookeeper会随机挑选（不是按照顺序）一个hostPort进行尝试，直到找到一个可以连接成功的host或者sessionTimeout
        * 入参格式如下：
        * hostport:
        *   host:port,host:port,host:port -- 针对集群
        *   host:port,host:port,host:port/app/a --针对需要初始默认的根目录情况，这个被称为chroot suffix
        * sessionTimeout：
        *   链接超时时间，单位毫秒
        * watcher：
        *   一个哨兵回调对象，用于监听节点状态的变更
        */
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dm.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }

    /***************************************************************************
     * WatchedEvent可以告诉你三个信息：
     *  1、发生了什么
     *  2、在那个znode路径发生的
     *  3、节点当前状态是什么
     * @see org.apache.zookeeper.Watcher#process(WatchedEvent)
     */
    @Override
    public void process(WatchedEvent event) {
        /*znode路径下节点状态变更*/
        dm.process(event);
    }

    /**
     * 节点数据发生变更
     * @param data
     */
    public void exists(byte[] data) {
        if (data == null) {
            /*数据不存在，关闭任务*/
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                /*关闭任务*/
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                /*将数据写入指定文件*/
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                /*启动任务*/
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                println(child.getInputStream(), System.out);
                println(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void println(InputStream inputStream, PrintStream printStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStreamReader inputStreamReader = null;
                try {
                    inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        printStream.println(line);
                    }
                } catch (Exception e) {
                    ;
                } finally {
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (Exception e) {
                            ;
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 节点无权、过期、不存在等情况
     * @param rc the ZooKeeper reason code
     */
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }
}
