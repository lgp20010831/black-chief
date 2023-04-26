package com.black.condition;

import com.black.condition.def.DefaultConditional;
import com.black.condition.def.DefaultConditionalOnClass;
import com.black.condition.def.DefaultConditionalOnExpression;
import com.black.condition.def.DefaultConditionalOnValue;
import com.black.condition.inter.ConditionalResolver;
import com.black.core.factory.beans.BeanFactory;
import lombok.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ConditionalEngine {

    private final BeanFactory beanFactory;

    private LinkedBlockingQueue<ConditionalResolver> resolvers;

    private List<Class<? extends ConditionalResolver>> resolverList;

    public ConditionalEngine(BeanFactory beanFactory, Consumer<List<Class<? extends ConditionalResolver>>> consumer) {
        this.beanFactory = beanFactory;
        resolverList = new ArrayList<>();
        resolverList.add(DefaultConditional.class);
        resolverList.add(DefaultConditionalOnClass.class);
        resolverList.add(DefaultConditionalOnValue.class);
        resolverList.add(DefaultConditionalOnExpression.class);
        if (consumer != null){
            consumer.accept(resolverList);
        }

    }

    public LinkedBlockingQueue<ConditionalResolver> getResolvers() {
        if (resolvers == null){
            resolvers = new LinkedBlockingQueue<>();
            for (Class<? extends ConditionalResolver> type : resolverList) {
                resolvers.add(beanFactory.getSingleBean(type));
            }
        }
        return resolvers;
    }

    @SuppressWarnings("ALL")
    public boolean resolveCondition(@NonNull AnnotatedElement element, Object source){
        for (ConditionalResolver resolver : getResolvers()) {
            if (resolver.support(element)) {
                if (!resolver.parse(element, source)){
                    return false;
                }
            }
        }
        return true;
    }
}
