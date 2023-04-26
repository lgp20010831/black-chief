package com.black.core.aop.code;

public class DefaultJoinHandler implements JoinInterceptToTaskChain{

    private final InterceptHijackWrapperFactory hijackWrapperFactory;

    public DefaultJoinHandler(InterceptHijackWrapperFactory hijackWrapperFactory) {
        this.hijackWrapperFactory = hijackWrapperFactory;
    }

    @Override
    public void addChain(AopProxyTaskChain chain, InterceptInitialInformation information) {
        InterceptHijackWrapper wrapper = hijackWrapperFactory.createWrapper(information.getIntercepet());
        chain.add(wrapper);
    }
}
