package com.black.treaty;

import com.black.nio.group.Session;

import java.util.Collection;

//协议网络调度器
// 主要作用: 管理连接, 接收请求, 调度请求, 返回响应
public interface TreatyNetworkScheduler {

    TreatyConfig getConfig();

    Session bind();

    void shutdown();

    boolean isShutdown();

    TreatyClient findClient(String id);

    TreatyClient findAddressClient(String address);

    Collection<TreatyClient> clients();
}
