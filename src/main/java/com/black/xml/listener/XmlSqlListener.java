package com.black.xml.listener;

import com.black.xml.XmlExecutor;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-30 11:20
 */
@SuppressWarnings("all")
public interface XmlSqlListener {


    default String postSelectSql(String sql, Map<String, Object> env, XmlExecutor xmlExecutor){
        return sql;
    }

    default String postUpdateSql(String sql, Map<String, Object> env, XmlExecutor xmlExecutor){
        return sql;
    }

}
