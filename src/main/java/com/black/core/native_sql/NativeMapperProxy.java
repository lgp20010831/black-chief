package com.black.core.native_sql;

import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.sql.NativeMapper;
import lombok.NonNull;

public class NativeMapperProxy implements AgentLayer {

    private final TransactionHandlerAndDataSourceHolder transactionHandlerAndDataSourceHolder;

    private final NativeMapperImpl impl;

    public NativeMapperProxy(@NonNull TransactionHandlerAndDataSourceHolder transactionHandlerAndDataSourceHolder) {
        this.transactionHandlerAndDataSourceHolder = transactionHandlerAndDataSourceHolder;
        impl = new NativeMapperImpl(
                transactionHandlerAndDataSourceHolder.getTransactionalDataSourceHandler(),
                transactionHandlerAndDataSourceHolder.getDataSource()
        );
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        MethodWrapper mw = layer.getProxyMethodWrapper();
        if (!checkMethod(mw)){
            return null;
        }
        return mw.invoke(impl, layer.getArgs());
    }

    private boolean checkMethod(MethodWrapper mw){
        return mw.getDeclaringClass().equals(NativeMapper.class);
    }
}
