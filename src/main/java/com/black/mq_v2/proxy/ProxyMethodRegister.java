package com.black.mq_v2.proxy;

import com.black.core.query.MethodWrapper;

public interface ProxyMethodRegister {

    void registerMethodObject(MethodWrapper mw, Object bean);

    void clear();
}
