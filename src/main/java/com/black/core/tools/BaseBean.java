package com.black.core.tools;

import com.black.JsonBean;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.json.Alias;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.ApplicationHolder;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.util.*;
import com.black.utils.ProxyUtil;
import com.black.utils.ReflexHandler;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/***
 * 作为可以继承的工具类
 */
public class BaseBean<T> extends JsonBean {

    public T complete(){
        return BeanUtil.fillBean((T) this);
    }

    public T wriedValue(){
        return (T) BeanUtil.wriedDefaultValue(this);
    }

    public boolean wasNullBean(){
        return BeanUtil.isNullBean(this);
    }

    public String obtainBeanName(){
        return StringUtils.titleLower(getClass().getSimpleName());
    }

    public Class<T> obtainClass(){
        return (Class<T>) BeanUtil.getPrimordialClass(getClass());
    }

    public ClassWrapper<T> obtainClassWrapper(){
        return ClassWrapper.get(obtainClass());
    }

    public Body fieldMap(){
        Class<BaseBean<T>> primordialClass = BeanUtil.getPrimordialClass(this);
        ClassWrapper<BaseBean<T>> cw = ClassWrapper.get(primordialClass);
        Collection<FieldWrapper> fields = cw.getFields();
        Body body = new Body();
        for (FieldWrapper fw : fields) {
            Object value = fw.getValue(this);
            Alias annotation = fw.getAnnotation(Alias.class);
            String name = annotation == null ? fw.getName() : annotation.value();
            body.put(name, value);
        }
        return body;
    }

    public T attributeCopy(T target){
        try {
            Class<?> beanClass = target.getClass();
            for (Field field : ReflexHandler.getAccessibleFields(this)) {
                Field declaredField = beanClass.getDeclaredField(field.getName());
                declaredField.setAccessible(true);
                declaredField.set(target, field.get(this));
            }
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
        return target;
    }

    public T wired(T data){
        ClassWrapper<T> classWrapper = obtainClassWrapper();
        ClassWrapper<?> dcw = BeanUtil.getPrimordialClassWrapper(data);
        for (FieldWrapper fw : classWrapper.getFields()) {
            if (dcw.getField(fw.getName()) != null) {
                SetGetUtils.invokeSetMethod(fw.get(), fw.getValue(data), this);
            }
        }
        return (T) this;
    }

    public T deepCory(){
        return (T) IoUtil.deepCopy(this);
    }

    public boolean wasProxyBean(){
        return ProxyUtil.isAopProxy(this);
    }

    public boolean wasSpringBean(){
        BeanFactory beanFactory = ApplicationHolder.getBeanFactory();
        if (beanFactory != null){
            return beanFactory.containsBean(obtainBeanName());
        }
        return false;
    }

    public T fillConfig(boolean force){
        ApplicationConfigurationReaderHolder.getReader().fullConfig(this, force);
        return (T) this;
    }

    public T fillConfig(){
        return fillConfig(false);
    }

    public T autoSet(Object... args){
        Method method = CurrentLineUtils.loadMethod(1);
        MethodWrapper mw = MethodWrapper.get(method);
        Map<String, Object> argMap = MapArgHandler.parse(args, mw);
        return (T) BeanUtil.mapping(this, argMap);
    }

    public T httpSet(){
        return (T) BeanUtil.httpSet(this);
    }
}
