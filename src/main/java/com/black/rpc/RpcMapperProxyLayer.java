package com.black.rpc;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.handler.ClientRequestParamCreator;
import com.black.rpc.inter.RemoteSocket;
import com.black.rpc.inter.ResponseHandler;
import com.black.rpc.log.Log;
import com.black.rpc.request.Request;
import com.black.rpc.response.Response;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.util.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class RpcMapperProxyLayer implements AgentLayer {

    private final RpcConfiguration configuration;

    public RpcMapperProxyLayer(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Method proxyMethod = layer.getProxyMethod();
        MethodWrapper mw = MethodWrapper.get(proxyMethod);
        Object[] args = layer.getArgs();
        ResponseDispatcher responseDispatcher = configuration.getResponseDispatcher();
        //create request
        Request request = createRequest(mw, args);
        //send request
        remoteRequest(request);
        //wait response
        Response response = responseDispatcher.getResponse(request.getRequestId());
        //handler response
        Object rpcResult = resolveResponse(response, mw);
        return rpcResult;
    }

    private Object resolveResponse(Response response, MethodWrapper mw) throws IOException {
        if (response == null){
            throw new IOException("获取响应超时");
        }
        Object result = null;
        LinkedBlockingQueue<ResponseHandler> responseHandlers = configuration.getResponseHandlers();
        for (ResponseHandler responseHandler : responseHandlers) {
            if (responseHandler.support(mw, response)){
                result = responseHandler.resolveResponse(mw, response);
                break;
            }
        }
        return result;
    }

    private void remoteRequest(Request request) throws IOException{
        Log log = configuration.getLog();
        RpcWebClientApplicationContext applicationContext = (RpcWebClientApplicationContext) configuration.getApplicationContext();
        RemoteSocket remoteSocket = applicationContext.getRemoteSocket();
        if (!remoteSocket.isConnected()) {
            log.debug("与服务连接已经断开, 尝试重新连接");
            remoteSocket.reconnect();
            Utils.sleep(configuration.getReconnectWaitTime());
            if (!remoteSocket.isConnected()){
                throw new IOException("无法重连至服务器");
            }
        }
        DataByteBufferArrayOutputStream out = remoteSocket.getOutputStream();
        byte[] requestBytes = request.toByteArray();
        out.write(requestBytes);
        out.flush();
    }


    private Request createRequest(MethodWrapper mw, Object[] args) throws IOException {
        ClientRequestParamCreator requestParamCreator = configuration.getRequestParamCreator();
        return requestParamCreator.createRequest(mw, args);
    }
}
