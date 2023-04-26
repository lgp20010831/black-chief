package com.black.rpc;

import com.black.rpc.log.Log;
import com.black.rpc.response.Response;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ResponseDispatcher {

    private final RpcConfiguration configuration;

    private Map<String, Thread> requestThreadMap = new ConcurrentHashMap<>();

    private Map<String, Response> responseMap = new ConcurrentHashMap<>();

    public ResponseDispatcher(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    public void putResponse(Response response){
        Log log = configuration.getLog();
        log.debug("调度响应: [{}]", response.getRequestId());
        String requestId = response.getRequestId();
        Thread waitThread = requestThreadMap.remove(requestId);
        if (waitThread == null){
            ResponseDispatcher.log.warn("Unclaimed response, request id is " + requestId);
            return;
        }
        responseMap.put(requestId, response);
        waitThread.interrupt();
    }

    public Response getResponse(String requestId){
        Log log = configuration.getLog();
        log.debug("等待响应: [{}]", requestId);
        requestThreadMap.put(requestId, Thread.currentThread());
        Response response = responseMap.get(requestId);
        if (response == null){
            try {
                Thread.sleep(configuration.getWaitResponseTime());
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        return response = responseMap.remove(requestId);
    }

}
