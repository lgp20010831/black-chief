package com.black.condition.def;

import com.black.condition.inter.ConditionalResolver;
import com.black.core.factory.beans.BeanFactory;

public abstract class AbstractConditionalResolver implements ConditionalResolver {

    protected final BeanFactory factory;

    public AbstractConditionalResolver(BeanFactory factory) {
        this.factory = factory;
    }
}
