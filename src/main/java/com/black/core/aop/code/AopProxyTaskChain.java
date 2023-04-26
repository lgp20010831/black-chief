package com.black.core.aop.code;



public class AopProxyTaskChain extends NeverAddedQueue<InterceptHijackWrapper> {

    public AopProxyTaskChain(InterceptHijackWrapper lastWrapper){
        super(lastWrapper);
    }

    public Object doInvoke(HijackObject hijackObject) throws Throwable {
        InterceptHijackWrapper wrapper = get(0);
        return wrapper.getIntercepet().processor(hijackObject);
    }
}
