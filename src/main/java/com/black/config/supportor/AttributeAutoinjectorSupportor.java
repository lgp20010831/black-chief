package com.black.config.supportor;

import com.black.config.AttributeValue;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.annotation.Attribute;
import com.black.core.query.FieldWrapper;
import com.black.core.util.StringUtils;

public class AttributeAutoinjectorSupportor extends AbstractAttributeSupportor {
    @Override
    public boolean supportField(FieldWrapper fw) {
        return fw.hasAnnotation(Attribute.class);
    }

    @Override
    public void pourintoField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        Attribute annotation = fw.getAnnotation(Attribute.class);
        String value = annotation.value();
        String key = StringUtils.hasText(value) ? value : fw.getName();
        AttributeValue attributeValue = autoinjector.selectAttributeValue(key);
        setValue(fw, key, bean, attributeValue, autoinjector);
    }
}
