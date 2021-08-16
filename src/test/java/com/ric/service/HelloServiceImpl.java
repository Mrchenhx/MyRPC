package com.ric.service;

import annotation.RpcService;

/**
 * @Author: Richard
 * @Create: 2021/08/16 16:25:00
 * @Description: 接口实现类
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}
