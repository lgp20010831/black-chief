package com.black.rpc;

import com.black.rpc.handler.ClientRequestParamCreator;
import com.black.rpc.inter.*;
import com.black.rpc.log.DefaultColorLog;
import com.black.rpc.log.Log;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.tools.BeanUtil;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

@Getter @Setter @SuppressWarnings("all")
public class RpcConfiguration {

    private Pattern pattern;

    private VfsScanner scanner;

    private Log log;

    private InetSocketAddress address;

    private BeanFactory beanFactory;

    private RpcWebApplicationContext applicationContext;

    private RpcMessageConvertHandler messageConvertHandler;

    private RpcMessageResolver<?> rpcMessageResolver;

    private ActuatorExecutor actuatorExecutor;

    private ResponseDispatcher responseDispatcher;

    private LinkedBlockingQueue<RequestParamResolver> requestParamResolvers;

    private LinkedBlockingQueue<RequestDistinctBuilder> requestDistinctBuilders;

    private LinkedBlockingQueue<ResponseHandler> responseHandlers;

    private LinkedBlockingQueue<RequestDeserializer> requestDeserializers;

    private LinkedBlockingQueue<JsonRequestHandler> requestHandlers;

    private ClientRequestParamCreator requestParamCreator;

    private long reconnectWaitTime = 700 * 1;

    //最大等待响应时间
    private long waitResponseTime = 1000 * 10;

    public RpcConfiguration(){
        FactoryManager.init();
        beanFactory = FactoryManager.getBeanFactory();
        scanner = VFS.findVfsScanner();
    }

    public void init(){
        log = new DefaultColorLog();
        messageConvertHandler = new DefaultMessageConvertHandler();
        actuatorExecutor = new DefaultActuatorExecutor(this);
        responseDispatcher = new ResponseDispatcher(this);
        requestParamCreator = new ClientRequestParamCreator(this);
        requestParamResolvers = new LinkedBlockingQueue<>();
        for (Class<?> type : scanner.load("com.black.rpc.handler.param")) {
            if (BeanUtil.isSolidClass(type) && RequestParamResolver.class.isAssignableFrom(type)){
                requestParamResolvers.add((RequestParamResolver) ReflexUtils.instance(type));
            }
        }
        requestDistinctBuilders = new LinkedBlockingQueue<>();
        for (Class<?> type : scanner.load("com.black.rpc.handler.builder")) {
            if (BeanUtil.isSolidClass(type) && RequestDistinctBuilder.class.isAssignableFrom(type)){
                requestDistinctBuilders.add((RequestDistinctBuilder) ReflexUtils.instance(type));
            }
        }
        responseHandlers = new LinkedBlockingQueue<>();
        for (Class<?> type : scanner.load("com.black.rpc.handler.response")) {
            if (BeanUtil.isSolidClass(type) && ResponseHandler.class.isAssignableFrom(type)){
                responseHandlers.add((ResponseHandler) ReflexUtils.instance(type));
            }
        }
        requestDeserializers = new LinkedBlockingQueue<>();
        for (Class<?> type : scanner.load("com.black.rpc.handler.deserializer")) {
            if (BeanUtil.isSolidClass(type) && RequestDeserializer.class.isAssignableFrom(type)){
                requestDeserializers.add((RequestDeserializer) ReflexUtils.instance(type));
            }
        }
        requestHandlers = new LinkedBlockingQueue<>();
        for (Class<?> type : scanner.load("com.black.rpc.handler.json")) {
            if (BeanUtil.isSolidClass(type) && JsonRequestHandler.class.isAssignableFrom(type)){
                requestHandlers.add((JsonRequestHandler) ReflexUtils.instance(type));
            }
        }
    }
}
