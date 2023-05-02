package com.black.core.sql.code.aop;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;
import com.black.core.aop.code.Premise;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.sql.annotation.EnabledMapSQLApplication;

public class SQLPremise implements Premise {
    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        return ChiefApplicationRunner.isPertain(EnabledMapSQLApplication.class);
    }
}
