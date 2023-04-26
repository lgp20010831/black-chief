package com.black.rpc;

import com.black.rpc.inter.RemoteSocket;
import com.black.rpc.response.Response;
import com.black.core.util.CentralizedExceptionHandling;

public class ResponseMessageReslover implements RpcMessageResolver<Response>{

    private final RpcConfiguration configuration;

    public ResponseMessageReslover(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void resolve(Response response, RemoteSocket remoteSocket) {
        try {
            ResponseDispatcher dispatcher = configuration.getResponseDispatcher();
            dispatcher.putResponse(response);
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}
