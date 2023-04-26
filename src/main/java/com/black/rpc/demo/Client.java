package com.black.rpc.demo;

import com.black.rpc.RpcClient;
import com.black.rpc.RpcWebClientApplicationContext;
import com.black.core.spring.util.ApplicationUtil;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException {
        RpcWebClientApplicationContext context = RpcClient.startClient(4000);
        UserMapper userMapper = context.proxyMapper(UserMapper.class);
        ApplicationUtil.programRunMills(() -> {
            System.out.println(userMapper.select("select * from driver where driver = ?1", null));
        });


        //context.shutdown();

    }
}
