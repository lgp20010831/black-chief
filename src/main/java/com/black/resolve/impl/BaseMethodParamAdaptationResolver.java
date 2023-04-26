package com.black.resolve.impl;

import com.black.resolve.annotation.ResolveSort;
import com.black.resolve.param.*;
import com.black.core.query.MethodWrapper;

@ResolveSort(700)
public class BaseMethodParamAdaptationResolver extends AbstractMethodParameterHandlerAdaptationResolver{

    @Override
    protected boolean concreteMethodSupport(MethodWrapper mw) {
        return true;
    }

    public BaseMethodParamAdaptationResolver(){
        registerParameter(new InputStreamHandler());
        registerParameter(new BytesParamHandler());
        registerParameter(new JsonBeanParamHandler());
        registerParameter(new UtfBodyHandler());
    }


}
