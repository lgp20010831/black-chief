package com.black.core.aop.servlet.result;

import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.util.Av0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HumpBodyHandler implements ResponseBodyHandler{


    @Override
    public Collection<?> processorCollection(HttpMethodWrapper httpMethodWrapper, Collection<?> collection) {
        return (Collection<?>) processorResult(collection);
    }

    @Override
    public Map<?, ?> processorMap(HttpMethodWrapper httpMethodWrapper, Map<?, ?> map) {
        return (Map<?, ?>) processorResult(map);
    }

    @Override
    public Object processorUnknown(HttpMethodWrapper httpMethodWrapper, Object result) {
        return processorResult(result);
    }

    protected Object processorResult(Object result){
        if (result instanceof RestResponse){
            RestResponse response = (RestResponse) result;
            return processorResult(response.obtainResult());
        }

        if (result instanceof Collection){
            Collection<?> collection = (Collection<?>) result;
            for (Object ele : collection) {
                processorResult(ele);
            }
        }

        if (result instanceof Map){
            processorMap((Map<String, Object>) result);
        }
        return result;
    }

    protected void processorMap(Map<String, Object> map){
        Set<String> keySet = new HashSet<>(map.keySet());
        for (String key : keySet) {
            Object val = processorResult(map.get(key));
            String ruacnlKey = Av0.ruacnl(key);
            if (!key.equals(ruacnlKey)){
                map.remove(key);
                map.put(ruacnlKey, val);
            }
        }
    }


}
