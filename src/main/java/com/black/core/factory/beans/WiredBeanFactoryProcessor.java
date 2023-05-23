package com.black.core.factory.beans;


import com.black.core.factory.beans.lazy.KeyUtils;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.utils.ReflexHandler;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import javax.print.ServiceUI;
import java.lang.reflect.Field;
import java.util.*;

@Log4j2
public class WiredBeanFactoryProcessor implements BeanInitializationHandler {

    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        return fw.hasAnnotation(WriedBean.class) && fw.isNull(bean);
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {
        final Class<?> type = fw.getType();
        final Field field = fw.getField();
        WriedBean annotation = fw.getAnnotation(WriedBean.class);
        if (Collection.class.isAssignableFrom(type)) {
            Class<?>[] genericVal = ReflexHandler.genericVal(fw.getField(), Collection.class);
            if (genericVal.length == 1){

                if (log.isDebugEnabled()) {
                    log.debug("Injected property object, detected that " +
                            "the field type is not a collection type, field name: [{}]", fw.getName());
                }
                Collection<?> objects = getBeans(genericVal[0], factory, fw, annotation.required());
                if (SetGetUtils.hasSetMethod(field)) {
                    SetGetUtils.invokeSetMethod(field, objects, bean);
                }else {
                    fw.setValue(bean, objects);
                }
            }
        }else if (Map.class.isAssignableFrom(type)){
            Class<?>[] genericVal = ReflexHandler.genericVal(fw.getField(), Map.class);
            if (genericVal.length == 2){

                if (log.isDebugEnabled()) {
                    log.debug("Injected property object, detected that " +
                            "the field type is not a map type, field name: [{}]", fw.getName());
                }
                //map key type
                Class<?> keyType = genericVal[0];
                //map value type
                Class<?> valType = genericVal[1];
                Collection<?> objects = getBeans(valType, factory, fw, annotation.required());
                Map<Object, Object> map = ServiceUtils.createMap(type);
                if (keyType.equals(String.class)){
                    Map<String, ?> handlerSource = KeyUtils.handlerKey(fw.get(), objects);
                    map.putAll(handlerSource);
                }else if (keyType.equals(Class.class)){
                    for (Object wriedBean : objects) {
                        BeanDefinitional<?> definitional = factory.getDefinitional(wriedBean);
                        if (definitional == null){
                            throw new BeanFactorysException("The identity definition of the object " +
                                    "obtained from the factory cannot be found, bean is [" + wriedBean + "]");
                        }

                        map.put(definitional.getPrimordialClass(), wriedBean);
                    }

                }else {
                    throw new BeanFactorysException("When the field attribute type is a map, " +
                            "either string or class as the index type is not supported by other types");
                }
                if (SetGetUtils.hasSetMethod(field)) {
                    SetGetUtils.invokeSetMethod(field, map, bean);
                }else {
                    fw.setValue(bean, map);
                }
            }
        }else {

            Object singleBean;
            Class<BeanFactory> factoryClass = BeanUtil.getPrimordialClass(factory);
            if (type.isAssignableFrom(factoryClass)){
                singleBean = factory;
            }else {
                singleBean = factory.getSingleBean(type);
            }
            if (SetGetUtils.hasSetMethod(field)) {
                SetGetUtils.invokeSetMethod(field, singleBean, bean);
            }else {
                fw.setValue(bean, singleBean);
            }
        }
    }

    protected Collection<?> getBeans(Class<?> type, BeanFactory factory, FieldWrapper fw, boolean required){
        try {
            return factory.getBean(type);
        }catch (BeanFactorysException bfe){
            if (!required){
                if (log.isDebugEnabled()) {
                    log.debug("When injecting the object field attribute, " +
                            "it is found that the attribute to be injected " +
                            "into the target field does not exist and cannot " +
                            "be created, but the attribute is not required, " +
                            "field is [{}]", fw.getName());
                }
                return new HashSet<>();
            }
            throw new BeanFactorysException("When injecting the object field attribute, " +
                    "it is found that the attribute to be injected into the target field " +
                    "does not exist, cannot be created, and is a required attribute, set field is [" +
                    fw.getName() + "]", bfe);
        }
    }
}
