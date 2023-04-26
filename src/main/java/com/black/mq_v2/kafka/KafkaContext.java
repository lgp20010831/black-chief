package com.black.mq_v2.kafka;

import com.black.mq_v2.core.AbstractAsyncMqttContext;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.mq_v2.definition.MqttStateCallBack;
import com.black.core.log.IoLog;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class KafkaContext extends AbstractAsyncMqttContext {

    private KafkaProducer<String, byte[]> producer;

    private KafkaConsumer<String, byte[]> consumer;

    private final Map<String, Object> producerProperties = new LinkedHashMap<>();

    private final Map<String, Object> consumerProperties = new LinkedHashMap<>();

    private final Set<String> topics = new HashSet<>();

    private MqttStateCallBack callback;

    private KafkaMessagePoller kafkaMessagePoller;

    public KafkaContext(){
        initProperties();
    }

    @Override
    protected Object buildSource() {
        if (producer != null || consumer != null){
            producer = new KafkaProducer<>(producerProperties);
            consumer = new KafkaConsumer<>(consumerProperties);
            if (!topics.isEmpty()){
                consumer.subscribe(topics);
            }
        }
        return producer;
    }

    private void initProperties(){
        producerProperties.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producerProperties.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        producerProperties.put("acks","all");
        producerProperties.put("retries",0);
        producerProperties.put("batch.size",16384);
        producerProperties.put("linger.ms",1);

        consumerProperties.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("group.id","con-1");
        consumerProperties.put("auto.offset.reset","latest");
        //自动提交偏移量
        consumerProperties.put("auto.commit.intervals.ms","true");
        consumerProperties.put("poll.timeout", 30);
    }

    @Override
    public void setServerHostAndPort(String host, int port) {
        super.setServerHostAndPort(host, port);
        producerProperties.put("bootstrap.servers", host + ":" + port);
        consumerProperties.put("bootstrap.servers", host + ":" + port);
    }

    @Override
    protected void doConnect() throws Throwable {
        if (kafkaMessagePoller == null || kafkaMessagePoller.isShutdown()){
            kafkaMessagePoller = new KafkaMessagePoller(this);
            kafkaMessagePoller.start();
        }
    }

    @Override
    protected void close0() {
        producer.close();
        consumer.close();
    }

    @Override
    public void subscribe(String... topics) {
        if (consumer == null){
            this.topics.addAll(Arrays.asList(topics));
        }else {
            consumer.subscribe(Arrays.asList(topics));
        }
    }



    @Override
    public void sendMessage(Message message, MessageSendCallback callback) {
        String topic = message.getTopic();
        byte[] body = message.getBody();
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, topic, body);
        try {
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (callback != null){
                        if (e != null){
                            try {
                                callback.onSuccess(message);
                            } catch (Throwable ex) {
                                log.error(ex);
                            }
                        }else {
                            callback.onFair(e, message);
                        }
                    }
                }
            });
        }catch (Throwable e){
            if (callback != null){
                callback.onFair(e, message);
            }
        }
    }

    @Override
    public void setStateCallback(MqttStateCallBack callback) {
        this.callback = callback;
    }

    @Override
    public void importProperties(Map<String, String> properties) {
        producerProperties.putAll(properties);
        consumerProperties.putAll(properties);
    }

    @Setter @Getter
    static class KafkaMessagePoller extends Thread{

        private final KafkaContext context;

        private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

        private final KafkaConsumer<String, byte[]> consumer;

        private boolean shutdown = false;

        KafkaMessagePoller(KafkaContext context) {
            this.context = context;
            consumer = context.getConsumer();
        }

        @Override
        public void run() {
            for (;;){
                IoLog ioLog = context.getLog();
                if (isShutdown()){
                    ioLog.debug("[{}] -- kafka message poller shutdown...", context.getName());
                    break;
                }
                Map<String, Object> properties = context.getConsumerProperties();
                Integer timeout = ServiceUtils.getInt(properties, "poll.timeout");
                timeout = timeout == null ? 30 : timeout;
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(timeout));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        Message message = castRecordToMessage(record);
                        MqttStateCallBack stateCallBack = context.getCallback();
                        if (stateCallBack != null){
                            try {
                                stateCallBack.messageArrived(message.getTopic(), message, context);
                            }catch (Throwable ex){
                                ioLog.error(ex);
                                ioLog.error("[{}] -- handler message: {} error", message.getTopic());
                            }
                        }
                    }
                }catch (Throwable e){
                    ioLog.error(e);
                    ioLog.error("[{}] -- kafka message poller polling fair, shutdown");
                    setShutdown(true);
                }
            }
        }

        Message castRecordToMessage(ConsumerRecord<String, byte[]> record){
            ByteMessage message = new ByteMessage();
            message.setTopic(record.topic());
            message.setBody(record.value());
            return message;
        }
    }
}
