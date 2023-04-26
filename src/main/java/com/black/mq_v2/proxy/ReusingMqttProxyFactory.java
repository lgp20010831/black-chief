package com.black.mq_v2.proxy;

import com.black.mq_v2.MqttUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.spring.factory.DefaultProxyFactory;
import com.black.core.spring.factory.ReusingProxyFactory;

public class ReusingMqttProxyFactory {

    private static final ReusingProxyFactory FACTORY = new DefaultProxyFactory();


    public static <T> T proxy(Class<T> type, MessageArrivedMethodRegister arrivedMethodRegister,
                              MessageSendProxyRegister sendProxyRegister){
        return proxy(type, null, null, arrivedMethodRegister, sendProxyRegister);
    }

    public static <T> T proxy(Class<T> type, Class<?>[] paramters, Object[] args,
                              MessageArrivedMethodRegister arrivedMethodRegister, MessageSendProxyRegister sendProxyRegister){
        T proxy = FACTORY.proxy(type, paramters, args, new AgentLayer() {
            @Override
            public Object proxy(AgentObject layer) throws Throwable {
                MethodWrapper methodWrapper = layer.getProxyMethodWrapper();
                Object result = layer.doFlow(layer.getArgs());
                sendProxyRegister.send(methodWrapper, layer.getProxyObject(), result);
                return result;
            }
        });
        MqttUtils.parseBean(proxy, arrivedMethodRegister);
        MqttUtils.parseBean(proxy, sendProxyRegister);
        return proxy;
    }
}
