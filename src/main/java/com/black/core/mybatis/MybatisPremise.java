package com.black.core.mybatis;

import com.black.core.chain.ChainPremise;
import com.black.core.spring.ChiefApplicationRunner;

public class MybatisPremise implements ChainPremise {
    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        if(mainClass != null){
            return mainClass.isAssignableFrom(EnableIbatisInterceptsDispatcher.class);
        }
        return false;
    }
}
