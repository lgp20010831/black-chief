package com.black.mq.client;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.mq.MQUtils;
import com.black.mq.handler.ActiveFinishHandler;
import com.black.mq.handler.ClientMessageHandler;
import com.black.mq.simple.*;
import com.black.socket.JHexSocket;
import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.Getter;

import java.io.IOException;
import java.util.Set;

@Getter
public class MQClient {

    private final JHexSocket socket;

    private boolean active = false;

    private final ActivationPackage activationPackage;

    private final MQBytesHandler bytesHandler;

    private MQCallBack callBack;

    private IoLog log;

    private Thread waitThread;

    private boolean autoReconnect = false;

    public MQClient(String host, int port){
        socket = new JHexSocket(host, port);
        activationPackage = new ActivationPackage();
        log = new CommonLog4jLog();
        bytesHandler = new MQBytesHandler(this);
        bytesHandler.registerHandler(new ActiveFinishHandler(this));
        bytesHandler.registerHandler(new ClientMessageHandler(this));
        socket.setJHexBytesHandler(bytesHandler);
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public void close() throws IOException {
        if (socket.isConnected()) {
            socket.close();
            if (callBack != null){
                callBack.connectLost(this);
            }
            if (autoReconnect){
                connectAndActive();
            }
        }
    }

    public boolean connectAndActive() throws IOException {
        boolean connect = connect();
        if (connect) {
            active();
        }
        return connect;
    }

    public boolean connect(){
        for (;;){
            try {
                doConnect();
            }catch (Throwable e){
                if (autoReconnect){
                    log.info("reconnect server");
                    continue;
                }
                return false;
            }
            if (callBack != null){
                callBack.connectComplete(this);
            }
            return true;
        }
    }

    private void doConnect() throws IOException {
        socket.connect(true);
    }

    public void setLog(IoLog log) {
        this.log = log;
    }

    public void setCallBack(MQCallBack callBack) {
        this.callBack = callBack;
    }

    public void registerTopics(String... topics){
        for (String topic : topics) {
            registerTopic(topic);
        }
    }

    public void registerTopic(String topic){
        activationPackage.registerTopic(topic);
    }

    public JHexSocket getSocket() {
        return socket;
    }

    public void finishActive(){
        if (waitThread != null){
            waitThread.interrupt();
        }
    }

    //激活客户端
    public void active() throws IOException {
        if (!active){
            doActive();
        }else {
            reActive();
        }
    }

    private void reActive() throws IOException {
        sendWaitFinishActivationPackage(DataType.RE_ACTIVE);
    }

    private void doActive() throws IOException {
        sendWaitFinishActivationPackage(DataType.ACTIVE);
        active = true;
    }

    private void sendWaitFinishActivationPackage(int dataType) throws IOException {
        //发送 active 指令
        JHexByteArrayOutputStream out = socket.getOutputStream();
        MQUtils.startOut(out);
        out.writeInt(dataType);
        out.writeHexJavaObject(activationPackage);
        MQUtils.endOut(out);
        waitThread = Thread.currentThread();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            //中断后, 表示激活成功
            log.info("finish active client");
            return;
        }finally {
            //清楚中断标志
            Thread.interrupted();
            waitThread = null;
        }
        throw new IOException("time out to active");
    }

    public void dispatchMessage(Message message) throws IOException {
        log.info("dispatch message: {}", message);
        if (MQUtils.isRequired(message)){
            sendMessageConfirm(message.messageId());
        }
        if (callBack != null) {
            Set<String> topics = message.topics();
            for (String topic : topics) {
                try {
                    callBack.messageReceived(topic, message);
                } catch (Throwable e) {
                    CentralizedExceptionHandling.handlerException(e);
                }
            }
        }
    }

    //发送消息确认数据包
    protected void sendMessageConfirm(String messageId) throws IOException {
        JHexByteArrayOutputStream out = getSocket().getOutputStream();
        MQUtils.startOut(out);
        out.writeInt(DataType.MESSAGE_COMFIRM);
        out.writeHexObject(messageId);
        MQUtils.endOut(out);
    }

    public void sendMessage(Object context, String... topics) throws IOException {
        sendMessage(context, 0, topics);
    }

    public void sendMessage(Object context, int ops, String... topics) throws IOException {
        SimpleMessage message = new SimpleMessage(context, ops);
        for (String topic : topics) {
            message.addTopic(topic);
        }
        sendMessage(message);
    }
    public void sendMessage(Message message) throws IOException {
        JHexByteArrayOutputStream outputStream = getSocket().getOutputStream();
        MQUtils.startOut(outputStream);
        outputStream.writeInt(DataType.MESSAGE);
        outputStream.writeHexJavaObject(message);
        MQUtils.endOut(outputStream);
    }

}
