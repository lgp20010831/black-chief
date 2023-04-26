package com.black.core.builder;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder {

    public static MapGovern<Object, Object> builder(){
        return new MapGovern<>();
    }

    public static <K, V> MapGovern<K, V> machining(Map<K, V> source){
        return new MapGovern<>(source);
    }


    public static class MapGovern<K, V>{

        final Map<K, V> map;

        public MapGovern(){
            this(new HashMap<>());
        }

        public MapGovern(Map<K, V> map){
            this.map = map;
        }

        public MapGovern<K, V> put(K key, V value){
            map.put(key, value);
            return this;
        }

        public MapGovern<K, V> putAll(Map<K, V> outSideMap){
            map.putAll(outSideMap);
            return this;
        }

        public Map<K, V> build(){
            return map;
        }
    }
}
