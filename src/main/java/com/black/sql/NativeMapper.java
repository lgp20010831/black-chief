package com.black.sql;

import com.black.core.native_sql.AliasStrategyThreadLocalManager;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;

import java.util.List;
import java.util.Map;

public interface NativeMapper {

    default NativeMapper setConvertHandler(AliasColumnConvertHandler handler){
        AliasStrategyThreadLocalManager.set(handler);
        return this;
    }

    default Map<String, Object> queryMap( String sql, Object... params){
        return SQLUtils.getSingle(queryList(sql, params));
    }

    default Map<String, Object> queryMap0(String sql, Map<String, Object> env, Object... params){
        return SQLUtils.getSingle(queryList0(sql, env, params));
    }

    default List<Map<String, Object>> queryList(String sql, Object... params){
        return queryList0(sql, null, params);
    }

    List<Map<String, Object>> queryList0(String sql, Map<String, Object> env, Object... params);

    default void update(String sql, Object... params){
        update0(sql, null, params);
    }

    void update0(String sql, Map<String, Object> env, Object... params);


}
