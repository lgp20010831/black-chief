package com.black.mq_v2.proxy;

import com.black.core.query.MethodWrapper;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ArrivedMethodBody extends MethodBody{

    public ArrivedMethodBody(MethodWrapper methodWrapper, Object invokeBean) {
        super(methodWrapper, invokeBean);
    }
}
