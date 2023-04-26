package com.black.mq_v2.proxy;

import com.black.mq_v2.MqttUtils;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MqttContext;
import com.black.mq_v2.definition.MqttStateCallBack;
import com.black.pattern.NameAndValue;
import com.black.core.log.IoLog;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class MessageArrivedMethodRegister implements ProxyMethodRegister{

    private final MqttContext context;

    private final LinkedBlockingQueue<ArrivedMethodBody> arrivedMethodBodies = new LinkedBlockingQueue<>();

    private final Map<Class<?>, NameAndValue> attributes;

    public MessageArrivedMethodRegister(MqttContext context) {
        this.context = context;
        attributes = MqttUtils.collectContextAttributes(context);
        Class<MqttContext> primordialClass = BeanUtil.getPrimordialClass(context);
        attributes.put(primordialClass, new NameAndValue("context", context));
        context.setStateCallback(new ArrivedMessageCallback());
    }

    public void registerBody(ArrivedMethodBody methodBody){
        if (!arrivedMethodBodies.contains(methodBody)) {
            Set<String> patterns = methodBody.getSupportPatterns();
            context.subscribe(patterns.toArray(new String[0]));
            arrivedMethodBodies.add(methodBody);
        }

    }

    public void registerMethodObject(MethodWrapper mw, Object bean){
        ArrivedMethodBody arrivedMethodBody = MqttUtils.parseMethodToArrivedBody(mw, bean);
        if (arrivedMethodBody != null){
            registerBody(arrivedMethodBody);
        }
    }

    public void clear(){
        arrivedMethodBodies.clear();
    }

    class ArrivedMessageCallback implements MqttStateCallBack {

        @Override
        public void messageArrived(String topic, Message message, MqttContext context) throws Throwable {
            List<ArrivedMethodBody> matchedArrivedMethods = matchArrivedMethod(message);
            if (!Utils.isEmpty(matchedArrivedMethods)){
                for (ArrivedMethodBody arrivedMethod : matchedArrivedMethods) {
                    IoLog log = context.getLog();
                    try {
                        log.debug("[{}] arrived message: {} --> {}", context.getName(),
                                message.getTopic(), arrivedMethod);
                        invokeArrivedMethod(arrivedMethod, message);
                    }catch (Throwable e){
                        log.error(e);
                        log.debug("[{}] -- An error occurred while executing message processing -- {} -- {}",
                                context.getName(), arrivedMethod.getMethodWrapper().getName(), ServiceUtils.getThrowableMessage(e, "unknown"));
                    }

                }
            }
        }

        private void invokeArrivedMethod(ArrivedMethodBody arrivedMethod, Message message){
            MethodWrapper methodWrapper = arrivedMethod.getMethodWrapper();
            Map<Class<?>, NameAndValue> attributesCopy = new LinkedHashMap<>(attributes);
            attributesCopy.put(ArrivedMethodBody.class, new NameAndValue("arrivedMethod", arrivedMethod));
            attributesCopy.put(MethodWrapper.class, new NameAndValue("methodWrapper", methodWrapper));

            Object invokeBean = arrivedMethod.getInvokeBean();
            Object[] args = MqttUtils.parseMessageToMethodArgArray(message, methodWrapper, attributesCopy);
            methodWrapper.invoke(invokeBean, args);
        }

        //根据当前消息去匹配哪些方法支持
        private List<ArrivedMethodBody> matchArrivedMethod(Message message){
            List<ArrivedMethodBody> bodies = new ArrayList<>();
            String topic = message.getTopic();
            for (ArrivedMethodBody methodBody : arrivedMethodBodies) {
                if (MqttUtils.matchArrivedBodySupportMessage(topic, methodBody)) {
                    bodies.add(methodBody);
                }
            }
            return bodies;
        }
    }

    @Override
    public String toString() {
        return StringUtils.letString("[", context.getName(), "]:", "\n", "arrived: ", arrivedMethodBodies);
    }
}
