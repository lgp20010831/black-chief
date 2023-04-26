package com.black.core.aop.servlet.plus;

import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MappingPolicyHandler {

    private final Map<MappingWrapper, String> cache = new ConcurrentHashMap<>();

    @Getter @Setter
    public static class MappingWrapper{
        String fieldName;
        MappingPolicy policy;

        public MappingWrapper(String fieldName, MappingPolicy policy) {
            this.fieldName = fieldName;
            this.policy = policy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MappingWrapper that = (MappingWrapper) o;
            return fieldName.equals(that.fieldName) && policy == that.policy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldName, policy);
        }
    }

    public String handlerByPolicy(String fieldName, MappingPolicy policy){
        MappingWrapper mappingWrapper = new MappingWrapper(fieldName, policy);
        String result = cache.get(mappingWrapper);
        if (result == null){
            switch (policy){
                case FieldName$ColumnName:
                    result =  fieldName;
                    break;
                case FieldName$column_name:
                    result = handlerUnderline(fieldName);
                    break;
                case FieldName$columnName:
                    result = handlerTitleLow(fieldName);
                    break;
            }
            cache.put(mappingWrapper, result);
        }
        return result;
    }

    protected String handlerTitleLow(String name){
        return StringUtils.titleLower(name);
    }

    protected String handlerUnderline(String name){
        char[] chars = name.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z'){
                builder.append("_");
                char i = (char) (c + 32);
                builder.append(i);
            }else {
                builder.append(c);
            }
            
        }

        String str = new String(builder);
        if (str.startsWith("_")){
            str = str.substring(1);
        }
        return str;
    }
}
