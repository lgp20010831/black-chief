package com.black.core.spring.annotation;



import com.black.core.spring.EnabledControlRisePotential;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoading {

    /***
     * 此注解作用于组件的类上,表示在启动期间不会去实例化
     * 该类, 该注解与{@link AddHolder} 冲突, 此注解优先级更高
     * 填充value 等同于实现{@link EnabledControlRisePotential}
     * 但此注解优先级更高
     */
    Class<? extends Annotation> value();
}
