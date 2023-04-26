package com.black.condition.def;

import com.black.condition.annotation.Conditional;
import com.black.condition.inter.Condition;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

public class DefaultConditional extends AbstractConditionalResolver{

    public DefaultConditional(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(Conditional.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        Conditional annotation = element.getAnnotation(Conditional.class);
        Class<? extends Condition>[] value = annotation.value();
        return doParse(value, factory, source);
    }

    public static boolean doParse(Class<? extends Condition>[] value, BeanFactory factory, Object source){
        if (value.length == 0){
            return true;
        }
        List<Condition> conditionList = new ArrayList<>();
        //实例化
        for (Class<? extends Condition> type : value) {
            conditionList.add(factory.getSingleBean(type));
        }

        for (Condition condition : conditionList) {
            if (!condition.test(source)){
                return false;
            }
        }
        return true;
    }
}
