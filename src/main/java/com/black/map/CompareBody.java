package com.black.map;

import com.black.core.util.Body;

import java.util.Map;
import java.util.Objects;


public class CompareBody extends Body {


    public CompareBody() {
    }

    public CompareBody(Map<String, Object> map) {
        super(map);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Map){
            if(size() != ((Map<?, ?>) obj).size()){
                return false;
            }
            for (String key : keySet()) {
                Object val = get(key);
                Object targetValue = ((Map<?, ?>) obj).get(key);
                if (!Objects.equals(val, targetValue)) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(obj);
    }
}
