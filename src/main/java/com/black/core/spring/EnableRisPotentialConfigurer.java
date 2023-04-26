package com.black.core.spring;

public interface EnableRisPotentialConfigurer {

    /***
     * 如果配置类实现了该接口, 然后可以在该方法
     * 上加上哪些注解,效果等于在实体类上加上注解
     * @return 返回要开启的哪些组件 class 对象
     */
    Class<? extends EnabledControlRisePotential>[] enableComponentMutes();
}
