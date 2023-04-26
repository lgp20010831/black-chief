package com.black.out_resolve.impl;

import com.black.out_resolve.OutputStreamResolver;

public abstract class AbstractOutputStreamResolver implements OutputStreamResolver {


    @Override
    public boolean support(Object rack) {
        return pareSupport(rack) && accurateSupport(rack);
    }


    protected String getStringValue(Object value){
        return value == null ? "" : value.toString();
    }

    protected boolean pareSupport(Object rack){
        return true;
    }

    protected boolean accurateSupport(Object rack){
        return false;
    }


}
