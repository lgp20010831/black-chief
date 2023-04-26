package com.black.core.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StreamHepler {


    public static List<String> mapVal(List<Map<String, Object>> maps, String key){
        return StreamUtils.mapList(maps, map -> MappingUtils.getString(map, key));
    }

    public static <T> Function<? super T, String> mapString(Map<String, Object> map, String key){
        return new StringGet<>(map, key);
    }

    public static class StringGet<T> implements Function<T, String> {

        private final Map<String, Object> map;
        private final String key;

        public StringGet(Map<String, Object> map, String key) {
            this.map = map;
            this.key = key;
        }

        @Override
        public String apply(Object o) {
            return MappingUtils.getString(map, key);
        }
    }

}
