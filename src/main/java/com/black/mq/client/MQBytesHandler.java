package com.black.mq.client;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.mq.MQUtils;
import com.black.mq.handler.MessageHandler;
import com.black.mq.server.MQInputStream;
import com.black.mq.simple.MessageHandlerRegister;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;
import com.black.core.log.IoLog;
import com.black.utils.IoUtils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.List;

@Log4j2
public class MQBytesHandler extends MessageHandlerRegister implements JHexBytesHandler {

    private final MQClient client;

    public MQBytesHandler(MQClient client) {

        this.client = client;
    }

    @Override
    public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
        log.info("read bytes: {}", in.available());
        List<JHexByteArrayInputStream> streams = MQUtils.unpack(in);
        String serverName = socket.getServerName();
        for (JHexByteArrayInputStream stream : streams) {
            doResolveBytes(stream, serverName);
        }
    }

    private void doResolveBytes(JHexByteArrayInputStream in, String address) throws Throwable {
        int type = in.readInt();
        byte[] bytes = IoUtils.readBytes(in);
        for (MessageHandler handler : getMessageHandlers()) {
            if (handler.support(type)) {
                handler.handler(new MQInputStream(bytes, address), type);
            }
        }
    }

    @Override
    public void handlerIoEvent(IOException ex, JHexSocket socket) throws IOException {
        client.close();
        IoLog log = client.getLog();
        log.info("mq has io error, close connect");
    }

    @Override
    public void handlerReadEvent(Throwable ex, JHexSocket socket) {
        try {
            client.close();
        } catch (IOException e) {}
        IoLog log = client.getLog();
        log.info("mq handle read bytes has error, close connect");
    }
}
