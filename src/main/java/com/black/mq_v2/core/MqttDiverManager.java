package com.black.mq_v2.core;

import com.black.mq_v2.MQTTException;
import com.black.mq_v2.definition.MqttContext;
import com.black.utils.ReflectionUtils;


public class MqttDiverManager {

    public static MqttContext load(MqttDriver driver){
        return load(driver.getPath());
    }

    public static MqttContext load(String path){
        try {
            Class<?> type = Class.forName(path);
            Object instance = ReflectionUtils.instance(type);
            if (!(instance instanceof MqttContext)){
                throw new IllegalStateException("is not mqtt driver: " + path);
            }
            return (MqttContext) instance;
        } catch (ClassNotFoundException e) {
            throw new MQTTException("no find mqtt driver", e);
        }
    }


}
