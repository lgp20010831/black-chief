package com.black.core.mybatis.source;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TransactionAopAdministrationWrapper {

    //事务开始的方法
    Method transactionStartMethod;

    //事务影响的数据源别名
    Set<String> transactionAffectDatasourceAlias = new HashSet<>();

    public TransactionAopAdministrationWrapper(Method transactionStartMethod, String... aliaies){
        this.transactionStartMethod = transactionStartMethod;
        transactionAffectDatasourceAlias.addAll(Arrays.asList(aliaies));
    }

    public Method getTransactionStartMethod() {
        return transactionStartMethod;
    }

    public Set<String> getTransactionAffectDatasourceAlias() {
        return transactionAffectDatasourceAlias;
    }
}
