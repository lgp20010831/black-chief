package com.black.mq.server;

import com.black.mq.MQUtils;
import com.black.mq.handler.ComfirmHandler;
import com.black.mq.handler.ServerActiveHandler;
import com.black.mq.handler.ServerMessageHandler;
import com.black.mq.simple.ActivationPackage;
import com.black.mq.simple.DataType;
import com.black.mq.simple.Message;
import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import lombok.Getter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class MQServer {

    private IoLog log;

    private long loopComfirmTime = 5000;

    private boolean close = false;

    private boolean executeLoop = false;

    private ScheduledFuture<?> future;

    protected final InputServer handlerRegister;

    //当接受到客户端连接后, 将客户端缓存
    protected final Map<String, MQOutputStream> connectClientCache = new ConcurrentHashMap<>();

    //激活后的客户端缓存
    protected final Map<String, Map<String, MQOutputStream>> clients = new ConcurrentHashMap<>();

    protected final Map<String, MessageChannelWrapper> waitConfirmationQueue = new ConcurrentHashMap<>();

    private static class MessageLoop implements Runnable{

        private final MQServer server;

        private MessageLoop(MQServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            if (server.isClosed()){
                throw new IllegalStateException("server is closed");
            }
            IoLog log = server.getLog();
            Map<String, MessageChannelWrapper> waitConfirmationQueue = server.getWaitConfirmationQueue();
            for (MessageChannelWrapper wrapper : waitConfirmationQueue.values()) {
                Message message = wrapper.getMessage();
                log.debug("resend message:{}", message.messageId());
                server.doSendClient(wrapper.getOut(), message);
            }
        }
    }

    public MQServer(){
        log = new CommonLog4jLog();
        handlerRegister = instanceHandlerRegister();
        handlerRegister.registerHandler(new ComfirmHandler(this));
        handlerRegister.registerHandler(new ServerActiveHandler(this));
        handlerRegister.registerHandler(new ServerMessageHandler(this));
    }

    public void bind(String host, int port) throws IOException {
        bind0(host, port);
        executeLoopTask();
    }

    public void close() throws IOException {
        if (!isClosed()){
            close = true;
            if (!future.isCancelled()) {
                future.cancel(true);
                executeLoop = false;
            }
            doCloseServer();
        }
    }

    public boolean isClosed(){
        return close;
    }

    private void executeLoopTask(){
        if (!executeLoop){
            executeLoop = true;
            future = AsynGlobalExecutor.scheduleAtFixedRate(new MessageLoop(this), new Date().getTime(), loopComfirmTime, TimeUnit.MILLISECONDS);
        }
    }

    protected InputServer instanceHandlerRegister(){
        return new InputServer(this);
    }

    public InputServer getHandlerRegister() {
        return handlerRegister;
    }

    abstract void bind0(String host, int port) throws IOException;

    abstract void doCloseServer() throws IOException;

    public void confirmMessage(String messageId){
        log.info("confirm message: {}", messageId);
        waitConfirmationQueue.remove(messageId);
    }

    public void registerActiveClient(ActivationPackage acpack, String address){
        Set<String> topics = acpack.getTopics();
        log.info("active client: {} and register topics:{}", address, topics);
        MQOutputStream out = connectClientCache.get(address);
        if (out == null){
            log.error(null, "unknown connect client:{}", address);
            return;
        }
        for (String topic : topics) {
            Map<String, MQOutputStream> clientQueue = clients.computeIfAbsent(topic, tp -> new ConcurrentHashMap<>());
            clientQueue.put(out.getAddress(), out);
        }
        try {
            sendFinish(out);
        } catch (IOException e) {
            removeAcitveClient(out.getAddress());
        }
    }

    public void registerClient(MQOutputStream outputStream){
        log.info("register client:{}", outputStream.getAddress());
        connectClientCache.put(outputStream.getAddress(), outputStream);
    }

    public MQOutputStream removeClient(String address){
        if (connectClientCache.containsKey(address)){
            log.info("remove client:{}", address);
            return connectClientCache.remove(address);
        }
        return null;
    }

    public void removeAcitveClient(String address){
        log.info("remove active client:{}", address);
        for (Map<String, MQOutputStream> map : clients.values()) {
            map.remove(address);
        }
    }

    public void sendFinish(MQOutputStream out) throws IOException {
        MQUtils.startOut(out);
        out.writeInt(DataType.FINISH);
        MQUtils.endOut(out);
    }


    public void pushMessage(Message message){
        log.info("rece message and push message:{}", message);
        Set<String> topics = message.topics();
        Map<MQOutputStream, Message> outMessageMap = new HashMap<>();
        for (String topic : topics) {
            Map<String, MQOutputStream> clients = this.clients.get(topic);
            //获取所有注册了该主题的客户端
            for (MQOutputStream client : clients.values()) {
                Message copyMsg = outMessageMap.computeIfAbsent(client, cli -> MQUtils.copyMessage(message));
                copyMsg.addTopic(topic);
            }
        }

        //整理好所有要发送的消息
        for (MQOutputStream out : outMessageMap.keySet()) {
            Message msg = outMessageMap.get(out);
            doSendClient(out, msg);
        }
    }

    protected void doSendClient(MQOutputStream out, Message message){
        log.info("send message:{} to client: {}", message.messageId(), out.getAddress());
        try {
            MQUtils.startOut(out);
            out.writeInt(DataType.MESSAGE);
            out.writeHexJavaObject(message);
            MQUtils.endOut(out);
            String messageId = message.messageId();
            if (MQUtils.isRequired(message) && !waitConfirmationQueue.containsKey(messageId)){
                waitConfirmationQueue.put(messageId, new MessageChannelWrapper(message, out));
            }
        }catch (Throwable e){

        }
    }
}
