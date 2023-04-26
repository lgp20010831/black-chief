package com.black.mq_v2;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.black.mq_v2.annotation.*;
import com.black.mq_v2.core.ByteMessage;
import com.black.mq_v2.definition.Message;
import com.black.mq_v2.definition.MqttContext;
import com.black.mq_v2.proxy.*;
import com.black.pattern.NameAndValue;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.utils.IoUtils;
import org.springframework.util.AntPathMatcher;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class MqttUtils {

    public static final String NAME_PREFIX = "CHIEF_MQTT_";

    public static final AtomicInteger sort = new AtomicInteger(0);

    public static final AntPathMatcher MATCHER = new AntPathMatcher();

    public static String createName(){
        return NAME_PREFIX + sort.incrementAndGet();
    }

    public static Object[] parseMessageToMethodArgArray(Message message, MethodWrapper mw, Map<Class<?>, NameAndValue> attributes){
        Object[] args = new Object[mw.getParameterCount()];
        byte[] body = message.getBody();
        String topic = message.getTopic();
        Collection<ParameterWrapper> parameterWrappers = mw.getParameterWrappersSet();
        if (args.length == 1){
            ParameterWrapper wrapper = new ArrayList<>(parameterWrappers).get(0);
            args[0] = castBodyBytesToType(message, wrapper.getType());
            return args;
        }

        if (args.length == 2){
            for (ParameterWrapper pw : parameterWrappers) {
                int index = pw.getIndex();
                String name = pw.getName();
                Class<?> type = pw.getType();
                if (name.equalsIgnoreCase("topic") || pw.hasAnnotation(Topic.class)){
                    if (!type.equals(String.class)){
                        throw new IllegalStateException("topic param type must is string");
                    }
                    args[index] = topic;
                }else {
                    Object value = castBodyBytesToType(message, type);
                    args[index] = value;
                }
            }
            return args;
        }

        for (ParameterWrapper pw : parameterWrappers) {
            int index = pw.getIndex();
            String name = pw.getName();
            Class<?> type = pw.getType();
            if (pw.hasAnnotation(Topic.class)){
                if (!type.equals(String.class)){
                    throw new IllegalStateException("topic param type must is string");
                }
                args[index] = topic;
            }else
            if (pw.hasAnnotation(MqttBody.class)){
                Object value = castBodyBytesToType(message, type);
                args[index] = value;
            }else
            if (type.equals(String.class) && "topic".equalsIgnoreCase(name)){
                args[index] = topic;
            }else
            {
                String prepareName = null;
                Object prepareArg = null;
                if (attributes == null){
                    args[index] = null;
                    continue;
                }
                for (Class<?> attributeType : attributes.keySet()) {
                    if (!attributeType.equals(Object.class) && type.isAssignableFrom(attributeType)){
                        NameAndValue nameAndValue = attributes.get(attributeType);
                        String attrName = nameAndValue.getName();
                        Object value = nameAndValue.getValue();
                        //如果预备参数不为空, 则存在冲突
                        if (prepareArg != null){

                            if (prepareName == null){
                                //如果当前名称与参数名称直接匹配则替换
                                if (name.equals(attrName)) {
                                    prepareName = attrName;
                                    prepareArg = value;
                                }else {
                                    //俩者无法比较
                                    throw new IllegalStateException("Unable to compare which parameter to choose -- " + name);
                                }
                            }
                        }else {
                            if (name.equals(attrName)){
                                prepareName = attrName;
                            }
                            prepareArg = value;
                        }
                    }
                }
                args[index] = prepareArg;
            }
        }
        return args;
    }

    public static String[] getMqttName(Class<?> clazz){
        Mqtt annotation = findAnnotation(clazz, Mqtt.class);
        return annotation == null ? new String[]{null} : annotation.value();
    }

    public static <T> T castBodyBytesToType(Message message, Class<T> type){
        byte[] body = message.getBody();
        if (Message.class.isAssignableFrom(type)){
            return (T) message;
        }
        if (byte[].class.equals(type)){
            return (T) body;
        }

        if (String.class.equals(type)){
            return (T) new String(body);
        }
        String bodyStr = new String(body);
        return TypeUtils.cast(bodyStr, type, ParserConfig.getGlobalInstance());
    }

    public static ArrivedMethodBody parseMethodToArrivedBody(MethodWrapper mw, Object bean){
        MqttArrived annotation = findAnnotation(mw, MqttArrived.class);
        if (annotation == null){
            return null;
        }
        ArrivedMethodBody methodBody = new ArrivedMethodBody(mw, bean);
        String[] patterns = annotation.value();
        if (patterns.length == 0){
            String name = mw.getName();
            name = StringUtils.removeIfStartWithsIgnoreCase(name, "arrived", "receive", "get");
            String[] names = name.split("And");
            for (String n : names) {
                methodBody.addPatterns(n.toLowerCase());
            }
        }else {
            methodBody.addPatterns(patterns);
        }
        return methodBody;
    }


    public static void parseBean(Object bean, ProxyMethodRegister proxyMethodRegister){
        if (bean == null){
            return;
        }

        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(bean);
        Collection<MethodWrapper> methods = classWrapper.getMethods();
        for (MethodWrapper methodWrapper : methods) {
            proxyMethodRegister.registerMethodObject(methodWrapper, bean);
        }

        if (proxyMethodRegister instanceof MessageSendProxyRegister){
            MessageSendProxyRegister sendProxyRegister = (MessageSendProxyRegister) proxyMethodRegister;
            for (MethodWrapper method : methods) {
                CallBackMethodBody success = parseCallBackSuccess(method, bean);
                if (success != null){
                    sendProxyRegister.registerSuccessBody(success);
                }

                CallBackMethodBody fair = parseCallBackFair(method, bean);
                if (fair != null){
                    sendProxyRegister.registerFairBody(fair);
                }
            }
        }
    }

    public static ByteMessage createByteMessage(byte[] content, String topic){
        ByteMessage message = new ByteMessage();
        message.setBody(content);
        message.setTopic(topic);
        return message;
    }

    public static byte[] castResourceToBytes(Object resource){
       return IoUtils.getBytes(resource, true);
    }

    public static CallBackMethodBody parseCallBackFair(MethodWrapper mw, Object bean){
        CallBackOnFair annotation = findAnnotation(mw, CallBackOnFair.class);
        if (annotation == null) return null;
        CallBackMethodBody methodBody = new CallBackMethodBody(mw, bean);
        String[] patterns = annotation.value();
        if (patterns.length == 0){
            String name = mw.getName();
            name = StringUtils.removeIfStartWithsIgnoreCase(name, "fairOn", "fair", "errorOn", "error");
            String[] names = name.split("And");
            for (String n : names) {
                methodBody.addPatterns(n.toLowerCase());
            }
        }else {
            methodBody.addPatterns(patterns);
        }
        return methodBody;
    }

    public static CallBackMethodBody parseCallBackSuccess(MethodWrapper mw, Object bean){
        CallBackOnSuccess annotation = findAnnotation(mw, CallBackOnSuccess.class);
        if (annotation == null) return null;
        CallBackMethodBody methodBody = new CallBackMethodBody(mw, bean);
        String[] patterns = annotation.value();
        if (patterns.length == 0){
            String name = mw.getName();
            name = StringUtils.removeIfStartWithsIgnoreCase(name, "successOn", "success", "backOn", "back");
            String[] names = name.split("And");
            for (String n : names) {
                methodBody.addPatterns(n.toLowerCase());
            }
        }else {
            methodBody.addPatterns(patterns);
        }
        return methodBody;
    }

    public static SendMethodBody parseMethodToSendBody(MethodWrapper mw, Object bean){
        MqttPush annotation = findAnnotation(mw, MqttPush.class);
        if (annotation == null){
            return null;
        }
        Async async = findAnnotation(mw, Async.class);
        SendMethodBody methodBody = new SendMethodBody(mw, bean);
        String[] patterns = annotation.value();
        if (patterns.length == 0){
            String name = mw.getName();
            name = StringUtils.removeIfStartWithsIgnoreCase(name, "send", "push", "poll", "write");
            String[] names = name.split("And");
            for (String n : names) {
                methodBody.addPatterns(n.toLowerCase());
            }
        }else {
            methodBody.addPatterns(patterns);
        }
        methodBody.setAsync(async != null);
        return methodBody;
    }

    public static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> type){
        ClassWrapper<?> cw = ClassWrapper.get(clazz);
        T annotation = cw.getAnnotation(type);
        if (annotation == null){
            Set<Class<?>> fromClasses = cw.getAssignableFromClasses();
            for (Class<?> fromClass : fromClasses) {
                ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(fromClass);
                annotation = classWrapper.getAnnotation(type);
                if (annotation != null){
                    break;
                }
            }
        }
        return annotation;
    }


    public static <T extends Annotation> T findAnnotation(MethodWrapper mw, Class<T> type){
        T annotation = mw.getAnnotation(type);
        if (annotation == null){
            ClassWrapper<?> cw = mw.getDeclaringClassWrapper();
            annotation = cw.getAnnotation(type);
            if (annotation == null){
                Set<Class<?>> fromClasses = cw.getAssignableFromClasses();
                for (Class<?> fromClass : fromClasses) {
                    ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(fromClass);
                    annotation = classWrapper.getAnnotation(type);
                    if (annotation != null){
                        break;
                    }
                }
            }
        }
        return annotation;
    }

    public static Map<Class<?>, NameAndValue> collectContextAttributes(MqttContext context){
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(context);
        Collection<FieldWrapper> fields = wrapper.getFields();
        Map<Class<?>, NameAndValue> map = new LinkedHashMap<>();
        for (FieldWrapper field : fields) {
            Object value = field.getValue(context);
            NameAndValue nameAndValue = new NameAndValue();
            nameAndValue.setName(field.getName());
            nameAndValue.setValue(value);
            map.put(field.getType(), nameAndValue);
        }
        return map;
    }

    public static boolean matchArrivedBodySupportMessage(String topic, ArrivedMethodBody methodBody){
        return matchPatterns(topic, methodBody.getSupportPatterns());
    }

    public static boolean matchPatterns(String topic, Set<String> supportPatterns){
        for (String supportPattern : supportPatterns) {
            if (supportPattern.equals("*")){
                return true;
            }

            if (supportPattern.equals(topic)){
                return true;
            }

            if (MATCHER.match(supportPattern, topic)) {
                return true;
            }
        }
        return false;
    }


    public static boolean matchPattern(String path, String pattern){
        if (pattern.equals("*") || pattern.equals(path)){
            return true;
        }

        return MATCHER.match(pattern, path);
    }
}
