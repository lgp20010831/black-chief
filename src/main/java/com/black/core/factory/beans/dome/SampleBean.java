package com.black.core.factory.beans.dome;

import com.black.core.factory.beans.*;
import com.black.core.ill.aop.IllInterceptComponent;
import com.black.core.io.IoSerializer;
import com.black.core.mybatis.source.IbatisDynamicallyMultipleDatabasesComponent;

@AgentRequired
public class SampleBean {

    private final IbatisDynamicallyMultipleDatabasesComponent component;

    @WriedBean
    private IllInterceptComponent interceptComponent;

    public SampleBean(IbatisDynamicallyMultipleDatabasesComponent component) {
        this.component = component;
    }

    @PostConstructor
    void post(IoSerializer serializer){
        System.out.println("post constructor ---> " + serializer);
    }

    @InitMethod
    void init(){
        System.out.println("init method ----");
    }

    @DestroyMethod
    void lost(){
        System.out.println("destroy method");
    }

    public void we(){
        System.out.println("we执行");
    }
}
