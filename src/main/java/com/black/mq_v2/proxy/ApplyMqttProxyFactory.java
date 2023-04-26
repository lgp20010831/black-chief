package com.black.mq_v2.proxy;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.mq_v2.MqttUtils;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.Method;

public class ApplyMqttProxyFactory {

    public static <T> T proxy(T bean, MessageArrivedMethodRegister arrivedMethodRegister, MessageSendProxyRegister sendProxyRegister){
        MqttUtils.parseBean(bean, arrivedMethodRegister);
        MqttUtils.parseBean(bean, sendProxyRegister);
        return ApplyProxyFactory.proxy(bean, new ApplyProxyLayer() {
            @Override
            public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
                Object result = template.invokeOriginal(args);
                MethodWrapper methodWrapper = MethodWrapper.get(method);
                Object templateBean = template.getBean();
                sendProxyRegister.parseAndSend(methodWrapper, templateBean, result);
                return result;
            }
        });
    }

}
