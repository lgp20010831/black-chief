package com.black.core.rpc;

import com.black.rpc.annotation.Actuator;
import com.black.core.rpc.annotation.HitpAction;

@HitpAction
public class Action1 {

    public Object invoke1(){
        return "invoke1result";
    }

    Object invoke2(){
        return "invoke2result";
    }

    @Actuator
    private Object invoke3(){
        return "invoke3result";
    }
}
