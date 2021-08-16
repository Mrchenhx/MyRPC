package com.ric;

import client.RpcProxy;
import com.ric.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Richard
 * @Create: 2021/08/16 16:24:00
 * @Description: 客户端测试
 */
public class HelloServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServiceTest.class);

    public static void main(String[] args) throws InterruptedException {
        RpcProxy rpcProxy = new RpcProxy();
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("World");
        Thread.sleep(2000);
        LOGGER.info(result);
        Thread.sleep(2000);
        LOGGER.info(result);
    }
}
