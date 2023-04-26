package com.black.core.mq_v2;

import com.black.mq.client.MQClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Log4j2
//@ChiefServlet("mq")
public class MqController {

    MQClient client;

    public MqController() throws IOException {
        client = new MQClient("0.0.0.0", 9999);
        client.setCallBack((topic, message) -> {
            log.info("接到主题:{}, 消息:{}", topic, message);
        });
        client.registerTopics("a1", "a2", "a3");
        client.connectAndActive();
    }

    @GetMapping("send")
    void send(String topic, String msg) throws IOException {
        client.sendMessage(msg, topic);
    }


}
