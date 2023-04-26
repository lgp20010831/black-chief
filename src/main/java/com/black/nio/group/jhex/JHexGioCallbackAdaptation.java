package com.black.nio.group.jhex;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.nio.group.Configuration;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;

import java.io.IOException;

public class JHexGioCallbackAdaptation implements JHexBytesHandler {

    private final Configuration configuration;

    private final JHexSocket jHexSocket;

    private final GioContext gioContext;

    public JHexGioCallbackAdaptation(Configuration configuration, JHexSocket jHexSocket) {
        this.configuration = configuration;
        this.jHexSocket = jHexSocket;
        gioContext = new JHexGioAdaptationContext(jHexSocket, configuration);
    }

    public void connectFinish(){
        GioResolver resolver = configuration.getIntelligentResolver();
        resolver.connectCompleted(gioContext);
    }

    @Override
    public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
        GioResolver resolver = configuration.getIntelligentResolver();
        resolver.read(gioContext, in.readAll(), gioContext.getOutputStream());
    }

    @Override
    public void handlerReadEvent(Throwable ex, JHexSocket socket) {
        GioResolver resolver = configuration.getIntelligentResolver();
        resolver.trowable(gioContext, ex, gioContext.getOutputStream());
    }

    @Override
    public void handlerIoEvent(IOException ex, JHexSocket socket) throws IOException {
        GioResolver resolver = configuration.getIntelligentResolver();
        resolver.trowable(gioContext, ex, gioContext.getOutputStream());
    }
}
