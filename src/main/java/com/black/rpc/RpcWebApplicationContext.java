package com.black.rpc;

import lombok.NonNull;

public class RpcWebApplicationContext {


    protected final RpcConfiguration rpcConfiguration;

    public RpcWebApplicationContext(@NonNull RpcConfiguration configuration) {
        this.rpcConfiguration = configuration;
        configuration.setApplicationContext(this);
    }


    public void shutdown(){}

}
