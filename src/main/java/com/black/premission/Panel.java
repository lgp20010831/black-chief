package com.black.premission;

import com.black.core.sql.code.util.SQLUtils;

import java.util.List;
import java.util.Map;

public interface Panel<T> extends EntityPanel<T>{

    T findDataById(String id);

    List<T> dataList(T condition);

    default T singleList(T condition){
        return SQLUtils.getSingle(dataList(condition));
    }

    T join(T t);

    T dataSave(T t);

    boolean deleteData(String id);

    default boolean openEntity(){
        return true;
    }

    default List<T> dataListByMap(Map<String, Object> condition){
        throw new UnsupportedOperationException("dataListByMap");
    }

    default T singleListByMap(Map<String, Object> condition){
        return SQLUtils.getSingle(dataListByMap(condition));
    }

    default T joinByMap(Map<String, Object> t){
        throw new UnsupportedOperationException("joinByMap");
    }

    default T dataSaveByMap(Map<String, Object> t){
        throw new UnsupportedOperationException("dataSaveByMap");
    }



}
