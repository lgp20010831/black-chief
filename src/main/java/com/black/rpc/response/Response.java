package com.black.rpc.response;

import com.black.rpc.Rpc;
import com.black.rpc.RpcState;

public interface Response extends Rpc {

    RpcState getState();



}
