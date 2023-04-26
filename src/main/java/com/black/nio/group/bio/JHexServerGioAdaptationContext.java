package com.black.nio.group.bio;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.AbstractGioContext;
import com.black.nio.group.Configuration;
import com.black.nio.group.ContextType;
import com.black.nio.group.NioType;
import com.black.socket.JHexSocket;
import com.black.socket.server.JHexServerSocket;
import com.black.core.asyn.AsynGlobalExecutor;

public class JHexServerGioAdaptationContext extends AbstractGioContext {

    private final JHexSocket socket;

    private final JHexServerSocket serverSocket;

    private final Configuration configuration;

    public JHexServerGioAdaptationContext(JHexSocket socket, JHexServerSocket serverSocket, Configuration configuration) {
        super(NioType.JHEX, configuration, ContextType.SERVER);
        this.socket = socket;
        this.serverSocket = serverSocket;
        this.configuration = configuration;
    }

    public JHexServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void write(Object source) {
        castIosTask(() -> socket.write(source));
    }

    @Override
    public void flush() {
        castIosTask(socket::flush);
    }

    @Override
    public void shutdown() {
        castIosTask(socket::close);
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream() {
        return socket.getOutputStream();
    }

    @Override
    public String bindAddress() {
        return socket.getLocalName();
    }

    @Override
    public String remoteAddress() {
        return socket.getServerName();
    }

    @Override
    public Object source() {
        return socket;
    }

    @Override
    public void executeWork(Runnable runnable) {
        AsynGlobalExecutor.execute(runnable);
    }
}
