package com.black.core.aop.servlet.plus.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.black.core.aop.servlet.plus.*;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;

public class QueryWrapperHandler extends AbstractWrapperHandler{


    public QueryWrapperHandler(MappingPolicyHandler policyHandler) {
        super(policyHandler);
    }

    @Override
    public boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper) {
        return methodWrapper.getAnnotationMap().containsKey(WriedQueryWrapper.class) &&
                !methodWrapper.getAnnotationMap().containsKey(WriedUpdateWrapper.class);
    }

    @Override
    public Wrapper<?> handler(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper, QueryWrapperConfiguration configuration) {
        QueryWrapper<?> queryWrapper = new QueryWrapper<>();
        handlerObject(entryWrapper.getArg(), queryWrapper, entryWrapper, methodWrapper, configuration, null);
        return queryWrapper;
    }
}
