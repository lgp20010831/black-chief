package com.black.core.aop.code;

import org.springframework.aop.framework.adapter.AdvisorAdapter;

import java.util.Collection;

//他将注册到 aop 中
//其他的组件可以申请拦截的条件, 然后由 GlobalAopMamatchingDispatcher 去匹配
public interface GlobalAopMamatchingDispatcher extends AdvisorAdapter {

    /** 获取所有子节点 */
    Collection<AopTaskManagerHybrid> getHybrids();

    HijackObjectFactory obtainFactory();

    InterceptHijackWrapperFactory obtainWrapperFactory();
}
