package com.black.nio.code;

import com.black.nio.code.util.NioUtils;
import lombok.NonNull;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioClientContext extends AbstractNioContext {

    private EventLoopGroup clientGroup;
    private NioChannel nioChannel;
    public NioClientContext(@NonNull Configuration configuration) {
        super(configuration);
    }

    @Override
    public void start() throws IOException {
        //eventLoopGroup = createEventLoopGroup();
        try {
            clientGroup = new ClientEventLoopGroup(getConfiguration());
            connect();
        } catch (IOException e) {
            throw new AttysNioException(e);
        }
    }

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return clientGroup;
    }

    public NioChannel getNioChannel(){
        return nioChannel;
    }

    public void connect(){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(getConfiguration().getAddress());
            nioChannel = NioUtils.createNioChannel(socketChannel, getConfiguration());
            clientGroup.registerChannel(nioChannel, SelectionKey.OP_CONNECT);

        } catch (IOException e) {
            throw new AttysNioException(e);
        }
    }


    @Override
    protected EventLoopGroup createEventLoopGroup() throws IOException {
        Configuration configuration = getConfiguration();
        configuration.setGroupSize(1);
        return new DefaultEventLoopGroup(configuration);
    }
}
