package com.black.rpc;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.ill.NoMethodException;
import com.black.rpc.inter.ActuatorExecutor;
import com.black.rpc.inter.RemoteSocket;
import com.black.rpc.log.Log;
import com.black.rpc.request.Request;
import com.black.rpc.response.Response;
import com.black.rpc.socket.NioServerSocketTemplate;

import java.io.IOException;

public class RequestMessageResolver implements RpcMessageResolver<Request>{

    private final RpcConfiguration configuration;

    public RequestMessageResolver(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void resolve(Request request, RemoteSocket remoteSocket) {
        Log log = configuration.getLog();

        RpcWebServerApplicationContext applicationContext = (RpcWebServerApplicationContext) configuration.getApplicationContext();
        Response response;
        try {

            //寻找执行器
            MethodInvoker invoker = findMethodInvoker(request);

            //调用执行器
            ActuatorExecutor actuatorExecutor = configuration.getActuatorExecutor();
            Object execute = actuatorExecutor.execute(invoker, request.getParam(), request);
            response = RpcFormat.createOkResponse(request, execute);
        } catch (Throwable e) {
            log.error(e);
            response = RpcThrowableHandler.resolveThrowable(e, request, configuration);
        }

        if (response != null){
            writeResponse(response, remoteSocket);
            log.info("响应结束: {}", NioServerSocketTemplate.requestCount.get());

        }
    }

    private MethodInvoker findMethodInvoker(Request request) throws NoMethodException {
        RpcWebServerApplicationContext applicationContext = (RpcWebServerApplicationContext) configuration.getApplicationContext();
        Log log = configuration.getLog();
        RpcMethodRegister methodRegister = applicationContext.getMethodRegister();
        final String methodName = request.getMethodName();
        MethodInvoker invoker = methodRegister.getMethodInvoker(methodName);
        if (invoker == null){
            log.error("无法找到请求映射方法, 错误映射地址:{}", methodName);
            throw new NoMethodException("the request method could not be found");
        }
        log.info("方法执行器: {}", invoker);
        return invoker;
    }


    private void writeResponse(Response response, RemoteSocket remoteSocket){
        Log log = configuration.getLog();
        try {
            byte[] bytes = response.toByteArray();
            DataByteBufferArrayOutputStream out = remoteSocket.getOutputStream();
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            log.error("send has io error");
            log.error(e);
        }
    }


}
