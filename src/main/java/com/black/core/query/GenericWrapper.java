package com.black.core.query;

import java.lang.reflect.Type;

public interface GenericWrapper {


    default Type getGenericType(){
        throw new UnsupportedOperationException("can not get generic type");
    }

}
