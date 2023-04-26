package com.black.core.sql.code.condition.function;

import com.black.core.util.StringUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Collection;
import java.util.Map;

public class NotNullFunction extends BooleanFunction{

    @Override
    public String getName() {
        return "notNull";
    }

    private boolean notNull(Object... vals){
        for (Object val : vals) {
            if (!isNotNull(val)){
                return false;
            }
        }
        return true;
    }

    private boolean isNotNull(Object val){
        if (val instanceof Collection){
            Collection<?> collection = (Collection<?>) val;
            return collection != null && !collection.isEmpty();
        }

        if (val instanceof Map){
            Map<?, ?> map = (Map<?, ?>) val;
            return map != null && !map.isEmpty();
        }

        if (val instanceof String){
            return StringUtils.hasText((String) val);
        }
        return val != null;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        return condition(notNull(arg1.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        return condition(notNull(arg1.getValue(env), arg2.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        return condition(notNull(arg1.getValue(env), arg2.getValue(env), arg3.getValue(env)));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3, AviatorObject arg4) {
        return condition(notNull(arg1.getValue(env), arg2.getValue(env), arg3.getValue(env), arg4.getValue(env)));
    }
}
