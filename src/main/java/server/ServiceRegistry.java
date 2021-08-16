package server;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: Richard
 * @Create: 2021/08/10 22:55:00
 * @Description: 服务注册
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress){
        this.registryAddress = registryAddress;
    }

    // 服务注册
    public void register(String data){
        if(data != null){
            ZooKeeper zooKeeper = connectServer();
            if(zooKeeper != null){
                createNode(zooKeeper, data);
            }
        }
    }

    // 连接zookeeper服务器
    private ZooKeeper connectServer(){
        ZooKeeper zooKeeper = null;
        try {
            // 新建一个 zookeeper
            zooKeeper = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // 如果事件的状态是 连接状态，则释放门闩
                    if(event.getState() == Event.KeeperState.SyncConnected){
                        // 减少 “信号量”，当 “信号量” 为零的时候唤醒所有等待线程
                        latch.countDown();
                    }
                }
            });

            // 使当前线程等待直到闩锁倒计时为零，除非线程被中断
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return zooKeeper;
    }

    // 创建节点
    private void createNode(ZooKeeper zk, String data){

        try {
            // 将数据转为字节流
            byte[] bytes = data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH, bytes,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error("", e);
        }
    }
}
