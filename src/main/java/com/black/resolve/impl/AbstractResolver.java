package com.black.resolve.impl;

import com.black.resolve.InputStreamResolver;

public abstract class AbstractResolver implements InputStreamResolver {

    @Override
    public boolean support(Object rack) {
        return pareSupport(rack) && concreteSupport(rack);
    }

    protected boolean pareSupport(Object rack){
        return true;
    }

    protected boolean concreteSupport(Object rack){
        return false;
    }

}
