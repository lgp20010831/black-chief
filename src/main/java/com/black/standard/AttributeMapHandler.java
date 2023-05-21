package com.black.standard;

import com.black.utils.ServiceUtils;

import java.util.Map;

@SuppressWarnings("all")
public interface AttributeMapHandler extends AttributeAdaptation{

    Map<String, Object> getFormData();
    @Override
    default Object get(Object key){
        return getFormData().get(key);
    }

    @Override
    default Object getByExpression(String expression){
        return ServiceUtils.getByExpression(getFormData(), expression);
    }

}
