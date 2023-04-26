package com.black.nio.group.netty;


import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.AbstractGioContext;
import com.black.nio.group.ContextType;
import com.black.nio.group.NioType;
import com.black.nio.netty.Configuration;
import com.black.nio.netty.NettyContext;
import com.black.utils.IoUtils;
import io.netty.channel.ChannelHandlerContext;


public class NettyGioAdaptationContext extends AbstractGioContext {

    private final NettyContext context;

    private final ChannelHandlerContext chc;

    private final JHexByteArrayOutputStream outputStream;

    private final com.black.nio.group.Configuration configuration;

    public NettyGioAdaptationContext(NettyContext context, ChannelHandlerContext chc,
                                     JHexByteArrayOutputStream outputStream,
                                     com.black.nio.group.Configuration configuration) {
        super(NioType.NETTY, configuration, ContextType.SERVER);
        this.context = context;
        this.chc = chc;
        this.outputStream = outputStream;
        this.configuration = configuration;
    }

    @Override
    public void write(Object source) {
        byte[] bytes = IoUtils.getBytes(source, true);
        castIosTask(() -> outputStream.write(bytes));
    }

    @Override
    public void flush() {
        castIosTask(outputStream::flush);
    }

    @Override
    public void shutdown() {
        context.close();
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String bindAddress() {
        Configuration configuration = context.getConfiguration();
        return configuration.getHost() + "|" + configuration.getPort();
    }

    @Override
    public String remoteAddress() {
        return chc.channel().remoteAddress().toString();
    }

    @Override
    public Object source() {
        return context;
    }

    @Override
    public void executeWork(Runnable runnable) {
        context.execute(new com.black.function.Runnable() {
            @Override
            public void run() throws Throwable {
                runnable.run();
            }
        });
    }

    public ChannelHandlerContext getChc() {
        return chc;
    }
}
