package com.black.mq;

import com.black.mq.client.MQClient;
import com.black.mq.simple.MQCallBack;
import com.black.mq.simple.Message;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class ClientDemo {


    public static void main(String[] args) throws IOException {
        MQClient client = new MQClient("0.0.0.0", 9999);
        client.setCallBack(new MQCallBack() {
            @Override
            public void messageReceived(String topic, Message message) throws Throwable {
                log.info("接到主题:{}, 消息:{}, ", topic, message.content());
            }
        });
        client.registerTopic("lgp");
        client.setAutoReconnect(true);
        client.connectAndActive();
        client.sendMessage("测试消息", "lgp");
        client.sendMessage("无需确认测试消息", 1, "lgp");
    }
}
