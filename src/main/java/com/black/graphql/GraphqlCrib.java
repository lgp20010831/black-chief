package com.black.graphql;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.tools.BeanUtil;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;

import java.util.concurrent.LinkedBlockingQueue;

public class GraphqlCrib {

    final LinkedBlockingQueue<GraphqlHandler> handlers = new LinkedBlockingQueue<>();

    final BeanFactory factory;

    final ReusingProxyFactory proxyFactory;

    final VfsScanner vfsLoader;

    Log log = new SystemLog();

    public GraphqlCrib(){
        this(false);
    }

    public GraphqlCrib(boolean factoryInstance) {
        FactoryManager.init();
        factory = FactoryManager.getBeanFactory();
        proxyFactory = FactoryManager.getProxyFactory();
        vfsLoader = VFS.findVfsScanner();
        for (Class<?> type : vfsLoader.load("com.black.graphql.handler")) {
            if (BeanUtil.isSolidClass(type)){
                handlers.add(instance(type, factoryInstance));
            }
        }
    }

    private GraphqlHandler instance(Class<?> type, boolean factoryInstance){
        if (factoryInstance){
            return (GraphqlHandler) factory.getSingleBean(type);
        }else {
            return (GraphqlHandler) ReflexUtils.instance(type);
        }
    }

    public void registerHandler(GraphqlHandler handler){
        if (handler != null){
            handlers.add(handler);
        }
    }

    public LinkedBlockingQueue<GraphqlHandler> getHandlers() {
        return handlers;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    public <T> T getMapper(Class<T> type){
        if (!type.isInterface()){
            throw new IllegalStateException("type must is interface");
        }
        return proxyFactory.proxy(type, new GraphqlLayer(this, type));
    }
}
