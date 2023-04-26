package com.black.http;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.Socket;

@Log4j2
public class SocketTransitExecutor extends AbstractSocketTransitExecutor{

    @Override
    public boolean support(AimType aimType) {
        return aimType == AimType.SOCKET;
    }

    @Override
    protected Socket createSocket(Configuration configuration) throws IOException {
        return new Socket(configuration.getRemoteHost(), configuration.getRemotePort());
    }


}
