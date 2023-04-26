package com.black.rpc;

import com.black.rpc.inter.RemoteSocket;

public interface RpcMessageResolver<R extends Rpc> {


    void resolve(R rpc, RemoteSocket remoteSocket);


}
