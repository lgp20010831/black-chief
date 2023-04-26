package com.black.netty.branch;


import com.black.netty.NettySession;

public interface CloseSettlement {

    void handlerCloseSettlement(NettySession session) throws Exception;
}
