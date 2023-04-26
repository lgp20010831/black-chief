package com.black.core.query;

import com.black.core.tools.BeanUtil;

public class WrapperUtils {


    public static <T> ClassWrapper<T> getClass(T instance){
        return ClassWrapper.get(BeanUtil.getPrimordialClass(instance));
    }


}
