package com.black.mq_v2.core;

import com.black.mq_v2.MQTTException;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.core.log.IoLog;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractAsyncMqttContext extends AbstractMqttContext{

    protected  AsyncMessageQueueWorker asyncWorker;

    @Override
    public void sendAsyncMessage(Message message, MessageSendCallback callback) {
        if (!isAsync()){
            throw new MQTTException("not support async send message");
        }
        if (asyncWorker == null){
            asyncWorker = createWorker();
            asyncWorker.start();
        }
        asyncWorker.addMessage(message, callback);
    }

    protected AsyncMessageQueueWorker createWorker(){
        return new AsyncMessageQueueWorker(this);
    }

    @Setter @Getter
    static class AsyncMessageQueueWorker extends Thread{

        private final LinkedBlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<>();

        private volatile boolean shutdown = false;

        private final AbstractAsyncMqttContext mqttContext;

        AsyncMessageQueueWorker(AbstractAsyncMqttContext mqttContext) {
            this.mqttContext = mqttContext;
        }

        public void addMessage(Message message, MessageSendCallback callback){
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setMessage(message);
            wrapper.setCallback(callback);
            queue.add(wrapper);
        }

        @Override
        public void run() {
            for (;;){
                IoLog ioLog = mqttContext.getLog();
                if (isShutdown()){
                    ioLog.debug("[{}] -- mqtt async worker shutdown...", getName());
                    break;
                }

                MessageWrapper mw = queue.poll();
                if (mw == null){
                    continue;
                }

                mqttContext.sendMessage(mw.getMessage(), mw.getCallback());
            }
        }
    }

    @Getter @Setter
    static class MessageWrapper{
        private Message message;
        private MessageSendCallback callback;
    }
}
