package com.black.aviator.object;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorType;

import java.util.Map;

public class AviatorBean<T> extends AviatorObject {

    final T bean;

    public AviatorBean(T bean) {
        this.bean = bean;
    }


    @Override
    public int innerCompare(AviatorObject other, Map<String, Object> env) {
        if (other instanceof AviatorBean) {
            boolean b = bean.equals(((AviatorBean) other).bean);
            return b ? 0 : 1;
        }
        return 1;
    }

    @Override
    public AviatorType getAviatorType() {
        return AviatorType.JavaType;
    }

    @Override
    public Object getValue(Map<String, Object> env) {
        return bean;
    }
}
