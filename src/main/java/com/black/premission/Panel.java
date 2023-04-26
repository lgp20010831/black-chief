package com.black.premission;

import com.black.core.sql.code.util.SQLUtils;

import java.util.List;

public interface Panel<T> extends EntityPanel<T>{

    T findDataById(String id);

    List<T> dataList(T condition);

    default T singleList(T condition){
        return SQLUtils.getSingle(dataList(condition));
    }

    T join(T t);

    T dataSave(T t);

    boolean deleteData(String id);

}
