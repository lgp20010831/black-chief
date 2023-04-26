package com.black.nio.group;

public interface Session {

    /**
     * 返回配置类
     * @return 配置类
     */
    Configuration getConfiguration();

    /**
     * 关闭会话
     */
    void shutdown();

    /***
     * 写数据并且冲刷到通道中, 只支持当前模式为 socket 时
     * @param source 要写的数据
     */
    void writeAndFlushAsSocket(Object source);

    /**
     * 返回当前会话绑定的资源
     * 可能是以下示例:
     * {@link com.black.socket.JHexSocket}
     * {@link com.black.nio.code.NioServerContext}
     * {@link com.black.nio.netty.NettyServerContext}
     * @return 绑定的资源
     */
    Object source();
}
