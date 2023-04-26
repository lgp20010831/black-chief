package com.black.utils;

import com.black.core.util.Body;
import com.black.core.util.MathUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class VoMap extends Body {

    private volatile boolean master = false;

    private final String dataName;

    public static final String DEFAULT_DATA_NAME = "data";

    public VoMap(){
        this(DEFAULT_DATA_NAME, null);
    }

    public VoMap(Object data){
        this(DEFAULT_DATA_NAME, data);
    }

    public VoMap(String dataName, Object data){
        this.dataName = dataName;
        put(dataName, data);
    }

    public Object main(){
        return get(dataName);
    }

    public Object flipGet(Object key){
        return flip().get(key);
    }

    public VoMap flip(){
        master = true;
        return this;
    }

    @Override
    public Object get(Object key) {
        try {
            return super.get(key);
        }finally {
            master = false;
        }
    }

    public <T> VoMap addSum(String key, Number number){
        Object value = get(key);
        if (value instanceof Number){
            if (value instanceof Integer){
                value = (Integer)value + number.intValue();
            }else {
                value = MathUtils.add(Double.parseDouble(value.toString()), number.doubleValue());
            }
            put(key, value);
        }
        return this;
    }

    public <T> VoMap sumDoublePut(String key, Function<T, Number> function){
        Double sum = 0D;
        Object master = get(dataName);
        if (master != null){
            if (master instanceof Collection){
                for (T ele : ((Collection<T>) master)) {
                    Number number = function.apply(ele);
                    if(number != null){
                        sum = MathUtils.add(sum, number.doubleValue());
                    }
                }
            }else if (master instanceof Map){
                for (T ele : ((Map<String, T>) master).values()) {
                    Number number = function.apply(ele);
                    if (number != null){
                        sum = MathUtils.add(sum, number.doubleValue());
                    }
                }
            }
        }
        put(key, sum);
        return this;
    }

    public <T> VoMap sumIntPut(String key, Function<T, Number> function){
        Integer sum = 0;
        Object master = get(dataName);
        if (master != null){
            if (master instanceof Collection){
                for (T ele : ((Collection<T>) master)) {
                    Number number = function.apply(ele);
                    if(number != null){
                        sum += number.intValue();
                    }
                }
            }else if (master instanceof Map){
                for (T ele : ((Map<String, T>) master).values()) {
                    Number number = function.apply(ele);
                    if (number != null){
                        sum += number.intValue();
                    }
                }
            }
        }
        put(key, sum);
        return this;
    }
}
