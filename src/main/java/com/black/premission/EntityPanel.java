package com.black.premission;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.permission.TableNameUtils;

import java.util.Map;

public interface EntityPanel<T> {

    default String getTableName(){
        Class<T> entityType = entityType();
        if (entityType != null){
            return TableNameUtils.getTableName(entityType);
        }
        return null;
    }

    Class<T> entityType();


    default T convert(Map<String, Object> map){
        return JsonUtils.toBean(new JSONObject(map), entityType());
    }
}
