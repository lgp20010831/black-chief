package com.black.core.sql.code.condition;

import com.black.condition.annotation.ConditionalOnValue;
import com.black.condition.def.DefaultConditionalOnValue;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

public class SqlConditionalOnValue extends DefaultConditionalOnValue {

    public SqlConditionalOnValue(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        ConditionalOnValue annotation = element.getAnnotation(ConditionalOnValue.class);
        Map<String, Object> sourceMap = (Map<String, Object>) source;
        String[] expressions = annotation.value();
        for (String expression : expressions) {
            if (!ConditionSelector.excCondition(expression, sourceMap)) {
                return false;
            }
        }
        return true;
    }
}
