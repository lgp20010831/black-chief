package com.black.core.native_sql;

public interface NativeBlendSupportResolver {


    boolean support(String alias);

    TransactionHandlerAndDataSourceHolder obtainDataSource(String value) throws Throwable;
}
