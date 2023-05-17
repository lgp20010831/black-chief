package com.black.core.convert.v2;

import com.black.utils.TypeUtils;

/**
 * @author 李桂鹏
 * @create 2023-05-17 9:44
 */
@SuppressWarnings("all")
public class TypeUtilsBringRearTypeHandler implements BringRearTypeHandler{


    @Override
    public <T> T convert(Object val, Class<T> type) {
        return TypeUtils.castToJavaBean(val, type);
    }
}
