package com.ric;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import server.RpcServer;

/**
 * @Author: Richard
 * @Create: 2021/08/16 16:23:00
 * @Description: rpc 服务器端测试
 */
public class BootStrapServerTest {

    @Test
    public void startServer() {
        BasicConfigurator.configure();
        RpcServer server = new RpcServer();
        System.out.println(server.getActiveServiceName());
        server.start();
    }
}
