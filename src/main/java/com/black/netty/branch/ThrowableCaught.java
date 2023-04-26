package com.black.netty.branch;

import com.black.netty.NettySessionBoard;

public interface ThrowableCaught {

    void handlerThrowableCaught(NettySessionBoard slr, Throwable e);

}
