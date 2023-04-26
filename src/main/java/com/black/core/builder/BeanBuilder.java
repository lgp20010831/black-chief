package com.black.core.builder;

import com.black.core.json.ReflexUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;

import java.util.Map;

public class BeanBuilder {

    public static <B> BeanGovern<B> builder(Class<B> type){
        return new BeanGovern<>(type);
    }

    public static <B> B mapping(Class<B> type, Map<String, Object> map){
        return builder(type).set(map).build();
    }

    public static class BeanGovern<B>{
        final B instance;

        public BeanGovern(Class<B> type) {
            instance = ReflexUtils.instance(type);
        }

        public BeanGovern<B> set(String name, Object val){
            SetGetUtils.invokeSetMethod(name, val, instance);
            return this;
        }

        public BeanGovern<B> set(Map<String, Object> map){
            if (map != null){
                BeanUtil.mapping(instance, map);
            }
            return this;
        }

        public B build(){
            return instance;
        }
    }
}
