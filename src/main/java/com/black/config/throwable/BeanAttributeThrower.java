package com.black.config.throwable;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;

public interface BeanAttributeThrower {

    boolean supportField(FieldWrapper fw);


    void handlerThrowableField(FieldWrapper fw, Object bean, Throwable ex, ConfiguringAttributeAutoinjector autoinjector) throws Throwable;

    default boolean supportMethod(MethodWrapper mw){
        return false;
    };

    default void handlerThrowableMethod(MethodWrapper mw, Object bean, Throwable ex, ConfiguringAttributeAutoinjector autoinjector) throws Throwable{

    }


}
