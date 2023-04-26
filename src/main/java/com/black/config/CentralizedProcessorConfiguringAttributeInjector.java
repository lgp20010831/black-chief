package com.black.config;

import com.black.config.inferrer.ClassAttributeInferrer;
import com.black.config.intercept.AttributeSetIntercept;
import com.black.config.supportor.AttributeInjectorSupportor;
import com.black.config.throwable.BeanAttributeThrower;
import com.black.core.log.IoLog;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.utils.ServiceUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class CentralizedProcessorConfiguringAttributeInjector extends AbstractConfiguringAttributeAutoinjector{


    @Override
    public ConfiguringAttributeAutoinjector copy() {
        CentralizedProcessorConfiguringAttributeInjector injector = new CentralizedProcessorConfiguringAttributeInjector();
        injector.setAttributeSeparator(attributeSeparator);
        injector.setParseMethod(isParseMethod());
        return injector;
    }

    @Override
    public void pourintoBean(Object bean) {
        IoLog log = getLog();
        log.info("[CONFIG] pourinto bean: {}", bean);
        ClassWrapper<?> cw = BeanUtil.getPrimordialClassWrapper(bean);
        LinkedBlockingQueue<AttributeSetIntercept> intercepts = environment.getIntercepts();
        LinkedBlockingQueue<ClassAttributeInferrer> inferrers = environment.getInferrers();
        LinkedBlockingQueue<BeanAttributeThrower> attributeThrowers = environment.getAttributeThrowers();
        ConfiguringAttributeAutoinjector autoinjector = this;
        for (ClassAttributeInferrer inferrer : inferrers) {
            if (inferrer.support(cw)) {
                log.trace("[CONFIG] class inferrer infer: [{}] attributes",
                        AttributeUtils.getName(inferrer));
                Map<String, String> source = inferrer.infer(cw, bean, this);
                ConfiguringAttributeAutoinjector attributeAutoinjector = copy();
                attributeAutoinjector.setDataSource(source);
                autoinjector = attributeAutoinjector;
                break;
            }
        }


        LinkedBlockingQueue<AttributeInjectorSupportor> supportors = environment.getSupportors();
        for (FieldWrapper fw : cw.getFields()) {
            boolean volley = false;
            for (AttributeSetIntercept intercept : intercepts) {
                if (intercept.supportField(fw)) {
                    if (intercept.interceptField(fw, bean, autoinjector)) {
                        log.trace("[CONFIG] field intercept: [{}] on field: {}",
                                AttributeUtils.getName(intercept), fw.getName());
                        volley = true;
                        break;
                    }
                }
            }

            if (volley){
                continue;
            }

            for (AttributeInjectorSupportor supportor : supportors) {
                if (supportor.supportField(fw)) {
                    log.trace("[CONFIG] supportor handler: [{}] on field: {}",
                            AttributeUtils.getName(supportor), fw.getName());
                    try {
                        supportor.pourintoField(fw, bean, autoinjector);
                    }catch (Throwable ex){
                        for (BeanAttributeThrower thrower : attributeThrowers) {
                            if (thrower.supportField(fw)) {
                                try {
                                    thrower.handlerThrowableField(fw, bean, ex, autoinjector);
                                } catch (Throwable e) {
                                    throw new ConfigurerException(e);
                                }
                                break;
                            }
                        }
                        log.trace("[CONFIG] attribute thrower on field: {} -- fair on {}",
                                fw.getName(), ServiceUtils.getThrowableMessage(ex));
                    }
                }
            }
        }
        if (isParseMethod()){
            Collection<SetGetUtils.AccessMethod> setMethods = SetGetUtils.getSetMethods(cw.get());
            for (SetGetUtils.AccessMethod setMethod : setMethods) {
                MethodWrapper method = setMethod.getMethod();
                boolean volley = false;
                for (AttributeSetIntercept intercept : intercepts) {
                    if (intercept.supportSetMethod(method)) {
                        if (intercept.interceptMethod(method, bean, autoinjector)) {
                            log.trace("[CONFIG] method intercept: [{}] on field: {}",
                                    AttributeUtils.getName(intercept), method.getName());
                            volley = true;
                            break;
                        }
                    }
                }

                if (volley){
                    continue;
                }
                for (AttributeInjectorSupportor supportor : supportors) {
                    if (supportor.supportMethod(method)) {
                        log.trace("[CONFIG] supportor handler: [{}] on method: {}",
                                AttributeUtils.getName(supportor), method.getName());
                        try {
                            supportor.pourintoMethod(setMethod, bean, autoinjector);
                        }catch (Throwable ex){
                            for (BeanAttributeThrower thrower : attributeThrowers) {
                                if (thrower.supportMethod(method)) {
                                    try {
                                        thrower.handlerThrowableMethod(method, bean, ex, autoinjector);
                                    } catch (Throwable e) {
                                        throw new ConfigurerException(e);
                                    }
                                    break;
                                }
                            }
                            log.trace("[CONFIG] attribute thrower on method: {} -- fair on {}",
                                    method.getName(), ServiceUtils.getThrowableMessage(ex));
                        }
                    }
                }
            }
        }
    }
}
