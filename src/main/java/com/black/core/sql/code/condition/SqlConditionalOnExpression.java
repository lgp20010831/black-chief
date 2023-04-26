package com.black.core.sql.code.condition;

import com.black.condition.annotation.ConditionalOnExpression;
import com.black.condition.def.DefaultConditionalOnExpression;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

public class SqlConditionalOnExpression extends DefaultConditionalOnExpression {

    public SqlConditionalOnExpression(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        ConditionalOnExpression annotation = element.getAnnotation(ConditionalOnExpression.class);
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
