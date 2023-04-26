package com.black.netty.branch;

import com.black.netty.NettySession;

public interface StartComplete {

    void handlerStartComplete(NettySession ns) throws Exception;

}
