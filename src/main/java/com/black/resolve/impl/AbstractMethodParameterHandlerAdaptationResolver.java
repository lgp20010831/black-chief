package com.black.resolve.impl;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.resolve.inter.ParameterHandler;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractMethodParameterHandlerAdaptationResolver extends AbstractMethodElementResolver{

    protected final LinkedBlockingQueue<ParameterHandler> parameterHandlers = new LinkedBlockingQueue<>();

    protected void registerParameter(ParameterHandler parameterHandler){
        parameterHandlers.add(parameterHandler);
    }

    @Override
    protected Object resolveMethod(MethodWrapper mw, JHexByteArrayInputStream inputStream) throws Throwable {
        Collection<ParameterWrapper> parameterWrappers = mw.getParameterWrappersSet();
        Object[] args = new Object[mw.getParameterCount()];
        for (ParameterWrapper pw : parameterWrappers) {
            ParameterHandler targetHandler = null;
            for (ParameterHandler handler : parameterHandlers) {
                if (handler.support(pw)) {
                    targetHandler = handler;
                    break;
                }
            }
            Object arg = null;
            if (targetHandler != null){
                arg = targetHandler.doHandler(pw, inputStream);
            }
            args[pw.getIndex()] = arg;
        }
        return args;
    }
}
