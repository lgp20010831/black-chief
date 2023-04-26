package com.black.core.factory.beans;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;

import java.util.Map;

public class BeansUtils {



    public static <T> T wriedPropertiesBean(T bean, Map<String, String> global, boolean methodAdd){
        Class<T> primordialClass = BeanUtil.getPrimordialClass(bean);
        ClassWrapper<T> cw = ClassWrapper.get(primordialClass);
        for (FieldWrapper fw : cw.getFields()) {
            String name = fw.getName();
            if ((fw.isNull(bean) || ClassWrapper.isBasic(fw.getType().getName())) && global.containsKey(name)){
                String strVal = global.get(name);
                if (methodAdd){
                    SetGetUtils.invokeSetMethod(fw.getField(), strVal, bean);
                }else {
                    fw.setValue(bean, strVal);
                }

            }
        }
        return bean;
    }


}
