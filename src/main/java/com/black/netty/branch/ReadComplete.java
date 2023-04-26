package com.black.netty.branch;

import com.black.netty.NettySessionBoard;

public interface ReadComplete {


    void handlerReadComplete(NettySessionBoard slr) throws Exception;

}
