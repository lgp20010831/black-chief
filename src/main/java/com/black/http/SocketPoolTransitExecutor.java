package com.black.http;

import com.black.socket.pool.IdleSocketPool;
import com.black.socket.pool.SocketPoolConfiguration;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.Socket;

@Log4j2
public class SocketPoolTransitExecutor extends AbstractSocketTransitExecutor{

    private IdleSocketPool pool;

    public SocketPoolTransitExecutor() {

    }

    private synchronized void init(Configuration configuration){
        if (pool != null) return;
        SocketPoolConfiguration poolConfiguration = new SocketPoolConfiguration(configuration.getRemotePort());
        poolConfiguration.setServerHost(configuration.getRemoteHost());
        poolConfiguration.setMaxPoolSize(-1);
        pool = new IdleSocketPool(poolConfiguration);
    }

    @Override
    public boolean support(AimType aimType) {
        return aimType == AimType.POOL_SOCKET;
    }

    @Override
    protected Socket createSocket(Configuration configuration) throws IOException {
        init(configuration);
        return pool.getConnection();
    }

}
