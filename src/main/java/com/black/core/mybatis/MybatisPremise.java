package com.black.core.mybatis;

import com.black.core.chain.ChainPremise;
import com.black.core.spring.ChiefApplicationRunner;

public class MybatisPremise implements ChainPremise {
    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnableIbatisInterceptsDispatcher.class);
    }
}
