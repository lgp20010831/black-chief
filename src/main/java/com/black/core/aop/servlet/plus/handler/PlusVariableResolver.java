package com.black.core.aop.servlet.plus.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.black.core.aop.servlet.plus.EntryWrapper;
import com.black.core.aop.servlet.plus.PlusMethodWrapper;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;

public interface PlusVariableResolver {

    //返回true, 表示自己可以处理
    boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper);

    //具体的处理
    Wrapper<?> handler(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper, QueryWrapperConfiguration configuration);
}
