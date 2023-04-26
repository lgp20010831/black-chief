package com.black.config.intercept;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;

public interface AttributeSetIntercept {

    boolean supportField(FieldWrapper fw);

    boolean interceptField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector);

    default boolean supportSetMethod(MethodWrapper mw){
        return false;
    }

    default boolean interceptMethod(MethodWrapper mw, Object bean, ConfiguringAttributeAutoinjector autoinjector){
        return false;
    }
}
