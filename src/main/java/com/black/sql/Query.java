package com.black.sql;

import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.run.RunSqlParser;
import com.black.core.util.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Query implements Finish{

    String sql;

    Map<Object, Object> paramMap = new HashMap<>();

    Connection connection;

    Log log;

    Consumer<Connection> callback;

    Map<String, Object> env;

    public Query(String sql) {
        this.sql = sql;
    }

    public Query setEnv(Map<String, Object> env){
        this.env = env;
        return this;
    }

    public Query setParam(int index, Object param){
        paramMap.put(index, param);
        return this;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Query setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public Query clear(){
        paramMap.clear();
        return this;
    }

    public void setCallback(Consumer<Connection> callback) {
        this.callback = callback;
    }

    public void finish(){
        if (callback != null){
            callback.accept(connection);
        }
    }

    public Query setLog(Log log) {
        this.log = log;
        return this;
    }

    public String parseSql(){
        String sql = this.sql;
        if (env != null){
            paramMap.putAll(env);
        }
        Map<String, Object> copy = new HashMap<>();
        for (Object key : paramMap.keySet()) {
            copy.put(key.toString(), paramMap.get(key));
        }
        sql = RunSqlParser.parseSql(sql, copy);
        return doParseSql(sql, paramMap);
    }

    public static String doParseSqlWithSeq0(String sql, Map<Object, Object> paramMap){
        Map<String, Object> copy = new HashMap<>();
        for (Object key : paramMap.keySet()) {
            copy.put(key.toString(), paramMap.get(key));
        }
        sql = RunSqlParser.parseSql(sql, copy);
        return doParseSql(sql, paramMap);
    }

    public static String doParseSql(String sql, Map<Object, Object> paramMap){
        StringBuilder builder = new StringBuilder();
        char[] charArray = sql.toCharArray();
        boolean jump = false;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '?'){
                if (NativeUtils.isLegitimate(sql, i)) {
                    jump = true;
                    builder.append(wiredParam(sql, i, paramMap));
                    continue;
                }
            }
            if (!jump){
                builder.append(c);
            }else {
                jump = false;
            }
        }
        return builder.toString();
    }

    public static String wiredParam(String sql, int index, Map<Object, Object> paramMap){
        char paramIndexChar;
        if (index + 1 >= sql.length() || !Character.isDigit(paramIndexChar = sql.charAt(index + 1))){
            throw new NativeQueryException("sql ?后面需要指定参数下标, sql: " + sql + " 异常点: " + index);
        }
        int paramIndex = Integer.parseInt(String.valueOf(paramIndexChar));
        if (!paramMap.containsKey(paramIndex)) {
            throw new NativeQueryException("缺少参数下标定义: " + paramIndex + ", sql: " + sql);
        }
        return MapArgHandler.getString(paramMap.get(paramIndex));
    }

    public QueryResultSetParser execute(){
        Assert.notNull(connection, "缺少数据库连接");
        String sql = parseSql();
        if (log != null){
            if (log.isDebugEnabled()) {
                log.debug("==> query sql: " + sql);
            }
        }
        ResultSet resultSet = SQLUtils.runQuery(sql, connection);
        QueryResultSetParser parser = new QueryResultSetParser(resultSet);
        parser.setFinish(this);
        return parser;
    }

}
