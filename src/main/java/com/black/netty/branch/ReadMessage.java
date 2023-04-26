package com.black.netty.branch;

import com.black.netty.NettySessionBoard;

public interface ReadMessage {

    void handlerReadMessage(NettySessionBoard slr, String msg) throws Exception;

}
