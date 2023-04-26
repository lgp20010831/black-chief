package com.black.config.supportor;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.SetGetUtils;

public interface AttributeInjectorSupportor {


    boolean supportField(FieldWrapper fw);

    void pourintoField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector);


    default boolean supportMethod(MethodWrapper mw){
        return false;
    }

    default void pourintoMethod(SetGetUtils.AccessMethod mw, Object bean, ConfiguringAttributeAutoinjector autoinjector){

    }
}
