package com.black.share;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.nio.group.GioContext;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:43
 */
@SuppressWarnings("all")
public class ReflectionMappingShareServer extends ShareServer{


    public ReflectionMappingShareServer(String host, int port) {
        super(host, port);
    }

    protected Object getProcess(){
        return null;
    }

    @Override
    protected void cultivateMainBranch(GioContext context) {
        String newHost = parseHost(context.bindAddress());
        System.out.println("选中 leader: " + newHost);
        int newPort = port;
        if (host.equalsIgnoreCase(newHost)){
            newPort = port + 1;
        }
        Object process = getProcess();
        byte[] bytes = ShareUtils.createResponseBytes(new CulitivateBranchResponse(process, newPort));
        context.writeAndFlush(bytes);

        Map<String, GioContext> contextMap = getContextMap();
        contextMap.remove(context.bindAddress());


        CutBranchResponse cutBranchResponse = new CutBranchResponse(newHost, newPort);
        byte[] responseBytes = ShareUtils.createResponseBytes(cutBranchResponse);
        for (GioContext gioContext : contextMap.values()) {
            gioContext.writeAndFlush(responseBytes);
        }
    }

    protected String parseHost(String address){
        return address.split("|")[0];
    }

    @Override
    protected Object handleRequest(Request request) throws Throwable {
        InvokeMethodRequest invokeMethodRequest = (InvokeMethodRequest) request;
        return invokeMethod(invokeMethodRequest.getMethodName(), invokeMethodRequest.getArgs());
    }

    @Override
    public Object invokeMethod(String name, Object... args){
        Object process = getProcess();
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(process);
        MethodWrapper methodWrapper = classWrapper.getMethod(name, args.length);
        return methodWrapper.invoke(process, args);
    }
}
