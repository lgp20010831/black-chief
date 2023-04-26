package com.black.config.supportor;

import com.black.config.AttributeValue;
import com.black.config.ConfigurerException;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.annotation.MatchAttribute;
import com.black.core.query.FieldWrapper;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.Map;

public class MatchAttributeSupportor extends AbstractAttributeSupportor{

    private final AntPathMatcher matcher;

    public MatchAttributeSupportor() {
        matcher = new AntPathMatcher(".");
    }

    @Override
    public boolean supportField(FieldWrapper fw) {
        return fw.hasAnnotation(MatchAttribute.class);
    }

    @Override
    public void pourintoField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        MatchAttribute annotation = fw.getAnnotation(MatchAttribute.class);
        String[] values = annotation.value();
        Map<String, AttributeValue> pathAttributeValues = autoinjector.getPathAttributeValues();
        AttributeValue attributeValue = null;
        for (String value : values) {
            for (String path : pathAttributeValues.keySet()) {
                if (matcher.match(value, path)) {
                    attributeValue = pathAttributeValues.get(path);
                    break;
                }
            }
            if (attributeValue != null){
                break;
            }
        }

        if (attributeValue == null){
            throw new ConfigurerException("Unable to match path: " + Arrays.toString(values));
        }
        setValue(fw, attributeValue.getPath(), bean, attributeValue, autoinjector);
    }
}
