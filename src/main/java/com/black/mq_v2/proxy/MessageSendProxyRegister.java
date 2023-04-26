package com.black.mq_v2.proxy;

import com.black.mq_v2.MqttUtils;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MessageSendCallback;
import com.black.mq_v2.definition.MqttContext;
import com.black.pattern.NameAndValue;
import com.black.core.chain.GroupKeys;
import com.black.core.log.IoLog;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class MessageSendProxyRegister implements ProxyMethodRegister{

    private final MqttContext context;

    private final Map<GroupKeys, SendMethodBody> methodBodies = new ConcurrentHashMap<>();

    private final LinkedBlockingQueue<CallBackMethodBody> onSuccessCallbackQueue = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<CallBackMethodBody> onFairCallbackQueue = new LinkedBlockingQueue<>();

    private final IoLog log;

    private final Map<Class<?>, NameAndValue> attributes;

    public MessageSendProxyRegister(MqttContext context) {
        this.context = context;
        log = context.getLog();
        attributes = MqttUtils.collectContextAttributes(context);
        Class<MqttContext> primordialClass = BeanUtil.getPrimordialClass(context);
        attributes.put(primordialClass, new NameAndValue("context", context));
    }

    public void registerSuccessBody(CallBackMethodBody methodBody){
        if (!onSuccessCallbackQueue.contains(methodBody)){
            onSuccessCallbackQueue.add(methodBody);
        }

    }

    public void registerFairBody(CallBackMethodBody methodBody){
        if (!onFairCallbackQueue.contains(methodBody)){
            onFairCallbackQueue.add(methodBody);
        }
    }

    public void registerBody(SendMethodBody methodBody){
        GroupKeys groupKeys = new GroupKeys(methodBody.getMethodWrapper(), methodBody.getInvokeBean());
        methodBodies.put(groupKeys, methodBody);
    }

    public void registerSuccessMethodObject(MethodWrapper mw, Object bean){
        CallBackMethodBody methodBody = MqttUtils.parseCallBackSuccess(mw, bean);
        if (methodBody != null){
            registerSuccessBody(methodBody);
        }
    }

    public void registerFairMethodObject(MethodWrapper mw, Object bean){
        CallBackMethodBody methodBody = MqttUtils.parseCallBackFair(mw, bean);
        if (methodBody != null){
            registerFairBody(methodBody);
        }
    }

    public void registerMethodObject(MethodWrapper mw, Object bean){
        SendMethodBody methodBody = MqttUtils.parseMethodToSendBody(mw, bean);
        if (methodBody != null){
            registerBody(methodBody);
        }
    }

    public void clear(){
        methodBodies.clear();
    }

    public boolean isParsed(MethodWrapper mw, Object bean){
        GroupKeys groupKeys = new GroupKeys(mw, bean);
        return methodBodies.containsKey(groupKeys);
    }

    public void parseAndSend(MethodWrapper mw, Object bean, Object result){
        if (mw.getReturnType().equals(void.class)){
            return;
        }

        if (!isParsed(mw, bean)){
            registerMethodObject(mw, bean);
        }

        send(mw, bean, result);
    }

    public void send(MethodWrapper mw, Object bean, Object result){
        GroupKeys groupKeys = new GroupKeys(mw, bean);
        SendMethodBody methodBody = methodBodies.get(groupKeys);
        if (methodBody == null){
            log.debug("[{}] -- The current method is not registered -- {}", context.getName(), mw.getName());
            return;
        }

        SendCallBackBodyResolver callBackBodyResolver = new SendCallBackBodyResolver();
        try {
            byte[] bytes = MqttUtils.castResourceToBytes(result);
            Set<String> patterns = methodBody.getSupportPatterns();
            for (String pattern : patterns) {
                try {
                    ByteMessage message = MqttUtils.createByteMessage(bytes, pattern);
                    log.debug("[{}] -- send topic: {}, content: {} from {}", context.getName(),
                            message.getTopic(), message.getBody(), methodBody);
                    if (methodBody.isAsync()) {
                        context.sendAsyncMessage(message, callBackBodyResolver);
                    }else {
                        context.sendMessage(message, callBackBodyResolver);
                    }
                }catch (Throwable e){
                    log.debug("[{}] -- An error occurred while the agent was executing the send, target topic: {}",
                            context.getName(), pattern);
                }

            }
        }catch (Throwable e){
            log.debug("[{}] -- An error occurred in the overall sending logic", context.getName());
        }

    }


    class SendCallBackBodyResolver implements MessageSendCallback{


        @Override
        public void onSuccess(Message message) throws Throwable {
            String topic = message.getTopic();
            for (CallBackMethodBody methodBody : onSuccessCallbackQueue) {
                Set<String> patterns = methodBody.getSupportPatterns();
                if (MqttUtils.matchPatterns(topic, patterns)) {
                    try {
                        invokeMethodBody(message, methodBody, attributes);
                    }catch (Throwable e){
                        log.error(e);
                        log.debug("[{}] -- An error occurred while performing a send success callback -- {} -- {}",
                                context.getName(), methodBody.getMethodWrapper().getName(), ServiceUtils.getThrowableMessage(e, "unknown"));
                    }

                }
            }
        }

        private void invokeMethodBody(Message message, MethodBody methodBody, Map<Class<?>, NameAndValue> attributes){
            MethodWrapper methodWrapper = methodBody.getMethodWrapper();
            Object invokeBean = methodBody.getInvokeBean();
            Object[] args = MqttUtils.parseMessageToMethodArgArray(message, methodWrapper, attributes);
            methodWrapper.invoke(invokeBean, args);
        }

        @Override
        public void onFair(Throwable e, Message message) {
            String topic = message.getTopic();
            Map<Class<?>, NameAndValue> attributesCopy = new LinkedHashMap<>(attributes);
            attributesCopy.put(Throwable.class, new NameAndValue("throwable", e));
            for (CallBackMethodBody methodBody : onFairCallbackQueue) {
                Set<String> patterns = methodBody.getSupportPatterns();
                if (MqttUtils.matchPatterns(topic, patterns)) {
                    try {
                        invokeMethodBody(message, methodBody, attributesCopy);
                    }catch (Throwable ex){
                        log.debug("[{}] -- An error occurred while performing a send fair callback -- {} -- {}",
                                context.getName(), methodBody.getMethodWrapper().getName(), ServiceUtils.getThrowableMessage(ex, "unknown"));
                    }

                }
            }
        }
    }

    @Override
    public String toString() {
        return StringUtils.letString("[", context.getName(), "]:", "\n", "send: ", methodBodies.values(), "\n",
                "callBackOnSuccess: " , onSuccessCallbackQueue, "\n",
                "callBaclOnfair: ", onFairCallbackQueue);
    }
}
