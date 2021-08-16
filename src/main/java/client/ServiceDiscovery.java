package client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Constant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: Richard
 * @Create: 2021/08/11 10:51:00
 * @Description: 服务发现组件
 */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    // 存放服务地址
    private volatile List<String> serviceAddressList = new ArrayList<>();

    // 注册中心的地址
    private String registryAddress;

    public ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;
        ZooKeeper zooKeeper = connectServer();
        if(zooKeeper != null){
            watchNode(zooKeeper);
        }
    }

    // 发现服务
    public String discover(){
        String data = null;
        // 获取有效服务器的个数
        int size = serviceAddressList.size();
        if(size > 0){
            // 只有一个有效服务器
            if(size == 1){
                data = serviceAddressList.get(0);
                LOGGER.debug("unique service address : {}", data);
            }else{
                // 多个服务提供方，随机分配
                data = serviceAddressList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("choose an address : {}", data);
            }
        }
        return data;
    }

    // 连接zookeeper
    private ZooKeeper connectServer(){
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress,
                    Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return zooKeeper;
    }

    // 获取服务地址列表
    private void watchNode(final ZooKeeper zk) {
        try {
            //获取子节点列表
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
                    new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        //发生子节点变化时再次调用此方法更新服务地址
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            // System.out.println("子节点列表是否为空 " + (nodeList == null));
            for (String node : nodeList) {
                // System.out.println("当前节点名称 " + node);
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("node data: {}", dataList);
            this.serviceAddressList = dataList;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
