package com.black.mq.server;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.mq.MQUtils;
import com.black.mq.handler.MessageHandler;
import com.black.mq.simple.MessageHandlerRegister;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class InputServer extends MessageHandlerRegister {


    protected final MQServer server;

    protected Consumer<Throwable> throwableCallback;

    public InputServer(MQServer server) {
        this.server = server;
    }

    public void setThrowableCallback(Consumer<Throwable> throwableCallback) {
        this.throwableCallback = throwableCallback;
    }

    public Consumer<Throwable> getThrowableCallback() {
        return throwableCallback;
    }

    public void handleBytes(MQInputStream inputStream) throws IOException {
        List<JHexByteArrayInputStream> packs = MQUtils.unpack(inputStream);
        for (JHexByteArrayInputStream stream : packs) {
            try {

                doResolveBytes(stream, inputStream.getAddress());
            }catch (Throwable e){
                if (throwableCallback != null){
                    throwableCallback.accept(e);
                }
            }

        }
    }

    protected void doResolveBytes(JHexByteArrayInputStream inputStream, String address) throws Throwable{
        int type = inputStream.readInt();
        byte[] bytes = IoUtils.readBytes(inputStream);
        LinkedBlockingQueue<MessageHandler> messageHandlers = getMessageHandlers();
        for (MessageHandler handler : messageHandlers) {
            if (handler.support(type)) {
                handler.handler(new MQInputStream(bytes, address), type);
            }
        }

    }
}
