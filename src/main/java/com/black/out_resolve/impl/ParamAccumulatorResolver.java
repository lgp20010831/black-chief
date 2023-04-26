package com.black.out_resolve.impl;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.out_resolve.param.ParamOutputStreamResolver;
import com.black.pattern.InstanceQueue;

import java.lang.reflect.Parameter;
import java.util.Collection;


public class ParamAccumulatorResolver extends AbstractParamElementResolver{

    private final InstanceQueue<ParamOutputStreamResolver> queue;

    public ParamAccumulatorResolver() {
        queue = InstanceQueue.scan("com.example.out_resolve.param", false);
    }


    @Override
    protected void resolveParam(JHexByteArrayOutputStream outputStream, Parameter parameter, Object value) throws Throwable {
        Collection<ParamOutputStreamResolver> instances = queue.getInstances();
        for (ParamOutputStreamResolver instance : instances) {
            if (instance.support(parameter)) {
                instance.resolve(outputStream, parameter, value);
            }
        }
    }
}
