package com.black.rpc;

import com.black.nio.code.Configuration;
import com.black.nio.code.NioChannel;
import com.black.nio.code.NioClientContext;
import com.black.rpc.inter.RemoteSocket;
import com.black.rpc.socket.NioRemoteSocket;
import com.black.rpc.socket.NioRpcSocketTemplate;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.factory.ReusingProxyFactory;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class RpcWebClientApplicationContext extends RpcWebApplicationContext{

    private final ReusingProxyFactory proxyFactory;

    private RemoteSocket remoteSocket;

    private NioClientContext clientContext;

    private Set<Object> mapperCache = new HashSet<>();

    private Consumer<Configuration> nioConfigurationHook;


    public RpcWebClientApplicationContext(@NonNull RpcConfiguration configuration) {
        super(configuration);
        configuration.setRpcMessageResolver(new ResponseMessageReslover(configuration));
        proxyFactory = FactoryManager.initAndGetProxyFactory();
    }

    public Set<Object> getMapperCache() {
        return mapperCache;
    }

    public RemoteSocket getRemoteSocket() {
        return remoteSocket;
    }

    public <T> T proxyMapper(Class<T> type){
        if (!type.isInterface()) {
            throw new IllegalStateException("type must is interface");
        }
        if (proxyFactory.isAgent(type)) {
            return proxyFactory.getAlreadyProxiedObject(type);
        }else {
            T proxy = proxyFactory.proxy(type, new RpcMapperProxyLayer(rpcConfiguration));
            mapperCache.add(proxy);
            return proxy;
        }
    }

    @Override
    public void shutdown() {
        if (clientContext != null){
            clientContext.shutdownNow();
            clientContext = null;
            remoteSocket = null;
        }
    }

    public void connect() throws IOException {
        if (clientContext != null) return;
        InetSocketAddress address = rpcConfiguration.getAddress();
        Configuration configuration = new Configuration();
        configuration.setPort(address.getPort());
        configuration.setGroupSize(1);
        configuration.setHost(address.getHostName());
        configuration.setOpenWorkPool(true);
        configuration.setWriteInCurrentLoop(false);
        configuration.setChannelInitialization(pipeline -> {
            pipeline.addLast(new NioRpcSocketTemplate(rpcConfiguration));
        });
        if (nioConfigurationHook != null){
            nioConfigurationHook.accept(configuration);
        }
        clientContext = new NioClientContext(configuration);
        clientContext.start();
        NioChannel nioChannel = clientContext.getNioChannel();
        remoteSocket = new NioRemoteSocket(nioChannel);
    }

    public void setNioConfigurationHook(Consumer<Configuration> nioConfigurationHook) {
        this.nioConfigurationHook = nioConfigurationHook;
    }
}
