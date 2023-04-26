package com.black.config.supportor;

import com.black.config.AttributeValue;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;

public class NoAnnotationSupportor extends AbstractAttributeSupportor{
    @Override
    public boolean supportField(FieldWrapper fw) {
        return fw.getAnnotationTypes().size() == 0;
    }

    @Override
    public void pourintoField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        String key = fw.getName();
        AttributeValue attributeValue = autoinjector.selectAttributeValue(key);
        setValue(fw, key, bean, attributeValue, autoinjector);
    }

    @Override
    public boolean supportMethod(MethodWrapper mw) {
        return true;
    }

    @Override
    public void pourintoMethod(SetGetUtils.AccessMethod mw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        MethodWrapper method = mw.getMethod();
        String rawFieldName = mw.getRawFieldName();
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(bean);
        if (classWrapper.getField(rawFieldName) != null){
            //该字段已经被处理过了
            return;
        }
        AttributeValue attributeValue = autoinjector.selectAttributeValue(rawFieldName);
        Class<?> type = method.getParameterTypes()[0];
        Object poured = pour0(type, attributeValue, null, autoinjector);
        method.invoke(bean, poured);
    }
}
