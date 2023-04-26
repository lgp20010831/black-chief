package com.black.core.mq_v2;

import com.black.mq_v2.annotation.CallBackOnSuccess;
import com.black.mq_v2.annotation.MqttArrived;
import com.black.mq_v2.annotation.MqttBody;
import com.black.mq_v2.annotation.MqttPush;
import com.black.mq_v2.definition.MqttContext;
import com.black.utils.IdUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
//@ChiefServlet("l1")
public class LIZI1 {


    @MqttPush
    @GetMapping("send")
    String sendId(){
        return IdUtils.createShort8Id();
    }


    @MqttArrived
    void getId(String topic, @MqttBody String id, MqttContext context){
        System.out.println(context);
        log.info("受到消息: {} -- {}", topic, id);
    }


    @CallBackOnSuccess
    void successOnId(String topic){
        log.info("消息发送成功: {}", topic);
    }

}
