package com.black.netty.branch;

import com.black.netty.NettySessionBoard;

public interface LossConnection {

    void handlerLossConnection(NettySessionBoard slr) throws Exception;
}
