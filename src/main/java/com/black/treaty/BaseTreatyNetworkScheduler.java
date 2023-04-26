package com.black.treaty;

import com.black.nio.group.Factorys;
import com.black.nio.group.GioContext;
import com.black.nio.group.Session;
import com.black.nio.group.SessionFactory;
import com.black.core.log.IoLog;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseTreatyNetworkScheduler implements TreatyNetworkScheduler{

    private final TreatyConfig config;

    private Session session;

    private final Map<String, TreatyClient> gioRemoteCache = new ConcurrentHashMap<>();

    private final Map<String, TreatyClient> clientCache = new ConcurrentHashMap<>();

    public BaseTreatyNetworkScheduler(TreatyConfig config) {
        this.config = config;
    }

    void registerClient(GioContext context){
        IoLog log = config.getLog();
        String remoteAddress = context.remoteAddress();
        BaseTreatyClient treatyClient = new BaseTreatyClient(context);
        gioRemoteCache.put(remoteAddress, treatyClient);
        clientCache.put(treatyClient.getClientId(), treatyClient);
        log.info("[TREATY] 注册客户端: {} | {}", remoteAddress, treatyClient.getClientId());
    }

    void lost(GioContext context){
        IoLog log = config.getLog();
        String remoteAddress = context.remoteAddress();
        TreatyClient treatyClient = gioRemoteCache.remove(remoteAddress);
        if (treatyClient != null){
            clientCache.remove(treatyClient.getClientId());
            log.debug("[TREATY] 客户端: {} | {} 下线...", remoteAddress, treatyClient.getClientId());
        }
    }

    @Override
    public TreatyConfig getConfig() {
        return config;
    }

    @Override
    public Session bind() {
        SessionFactory sessionFactory = Factorys.open(config.getBindHost(), config.getBindPort(),
                new TreatyGioResolver(this), config.getNioType());
        sessionFactory.apply(configuration -> {
            configuration.setIoThreadNum(config.getIoThreadNum());
        });
        session = sessionFactory.openSession();
        config.getLog().trace("[TREATY] 服务启动成功, 绑定地址: {} | {}", config.getBindHost(), config.getBindPort());
        return session;
    }

    @Override
    public void shutdown() {
        if (session != null){
            session.shutdown();
        }
        session = null;
    }

    @Override
    public boolean isShutdown() {
        return session == null;
    }

    @Override
    public TreatyClient findClient(String id) {
        return clientCache.get(id);
    }

    @Override
    public TreatyClient findAddressClient(String address) {
        return gioRemoteCache.get(address);
    }

    @Override
    public Collection<TreatyClient> clients() {
        return clientCache.values();
    }
}
