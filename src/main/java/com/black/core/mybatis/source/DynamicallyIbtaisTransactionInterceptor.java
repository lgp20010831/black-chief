package com.black.core.mybatis.source;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

public class DynamicallyIbtaisTransactionInterceptor extends TransactionAspectSupport implements AgentLayer {



    @Override
    public Object proxy(AgentObject layer) {

        Class<?> proxyClazz = layer.getAgentClazz();

        try {
            return invokeWithinTransaction(layer.getProxyMethod(), proxyClazz, new CoroutinesInvocationCallback() {
                @Override
                @Nullable
                public Object proceedWithInvocation() throws Throwable {
                    return layer.doFlow(layer.getArgs());
                }
                @Override
                public Object getTarget() {
                    return layer.getProxyObject();
                }
                @Override
                public Object[] getArguments() {
                    return layer.getArgs();
                }
            });
        } catch (Throwable e) {
            CentralizedExceptionHandling.handlerException(e);
            throw new RuntimeException(e);
        }
    }
}
