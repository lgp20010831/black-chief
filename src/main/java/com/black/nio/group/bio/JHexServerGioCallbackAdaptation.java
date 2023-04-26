package com.black.nio.group.bio;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.nio.group.Configuration;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.socket.JHexSocket;
import com.black.socket.server.JHexServerSocket;
import com.black.socket.server.JHexSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JHexServerGioCallbackAdaptation implements JHexSocketHandler {

    private final Configuration configuration;

    private final JHexServerSocket serverSocket;

    private final Map<JHexSocket, GioContext> contextCache = new ConcurrentHashMap<>();


    public JHexServerGioCallbackAdaptation(Configuration configuration, JHexServerSocket serverSocket) {
        this.configuration = configuration;
        this.serverSocket = serverSocket;
    }

    public GioContext getContext(JHexSocket jHexSocket){
        return contextCache.computeIfAbsent(jHexSocket, s -> {
            return new JHexServerGioAdaptationContext(jHexSocket, serverSocket, configuration);
        });
    }

    @Override
    public void accept(JHexSocket socket, JHexServerSocket serverSocket) {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(socket);
        resolver.acceptCompleted(gioContext, gioContext.getOutputStream());
    }

    @Override
    public void handlerReadEvent(Throwable ex, JHexSocket socket) {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(socket);
        resolver.trowable(gioContext, ex, gioContext.getOutputStream());
    }

    @Override
    public void handlerIoEvent(IOException ex, JHexSocket socket) throws IOException {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(socket);
        resolver.trowable(gioContext, ex, gioContext.getOutputStream());
    }

    @Override
    public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket, JHexServerSocket serverSocket) throws Throwable {
        GioResolver resolver = configuration.getIntelligentResolver();
        GioContext gioContext = getContext(socket);
        resolver.read(gioContext, in.readAll(), gioContext.getOutputStream());
    }
}
