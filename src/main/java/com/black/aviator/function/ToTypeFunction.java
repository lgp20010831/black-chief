package com.black.aviator.function;

import com.black.aviator.object.AviatorBean;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.*;

import java.util.Map;

public class ToTypeFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "toType";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object value = arg1.getValue(env);
        Object type = arg2.getValue(env);
        String valueString = value == null ? "null" : value.toString();
        switch (type.toString()){
            case "string":
                return new AviatorString(valueString);
            case "int":
            case "double":
            case "long":
            case "short":
                return new AviatorDouble(Double.parseDouble(valueString));
            case "boolean":
                return Boolean.parseBoolean(valueString) ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
            case "byte":
            case "char":
                throw new UnsupportedOperationException("can not to byte or char");
            default:
                try {
                    Class<?> typeClass = Class.forName(valueString);
                    TypeHandler handler = TypeConvertCache.initAndGet();
                    return new AviatorBean<>(handler.convert(typeClass, value));
                } catch (ClassNotFoundException e) {
                    throw new UnsupportedOperationException("unknown type: " + valueString);
                }
        }
    }
}
