package com.black.core.sql.code;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Av0;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StreamUtils;
import com.black.sql.JdbcSqlUtils;
import com.black.sql.SqlOutStatement;
import com.black.utils.ServiceUtils;

import java.util.*;

public class MapArgHandler {

    public static final String START = "#{";

    public static final String END = "}";

    public static void main(String[] args) {
        System.out.println(parseSql("update ayc set ${u}, name = #{source.name} where ${d} and id in #{source.id} and #{source.cv}",
                Av0.of("source", Av0.of("name", "lgp", "id", Av0.as(15, 16, 7), "cv", Av0.js("del", false, "state", "1")))));
    }

    public static String parseSql(String sql, Map<String, Object> argMap){
        if (sql == null) return sql;

        sql = ServiceUtils.parseTxt(sql, START, END, key -> {
            Object v = getValue(argMap, key);
            return getString(v);
        });

        sql = ServiceUtils.parseTxt(sql, "^{", "}", key -> {
            Object value = getValue(argMap, key);
            return value == null ? "null" : value.toString();
        });

        return sql;
    }

    public static Map<String, Object> parse(Object[] args, MethodWrapper mw){

        if(args == null || args.length != mw.getParameterCount()){
            throw new IllegalArgumentException("ill args");
        }
        Map<String, Object> argMap = new HashMap<>();
        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            argMap.put(pw.getName(), args[pw.getIndex()]);
        }
        return argMap;
    }


    public static Object getValue(Map<String, Object> argMap, String entry){
        if (entry == null) return null;
        Object val = argMap;
        for (String e : entry.split("\\.")) {
            if (val == null) return null;
            if (val instanceof Map){
                Map<String, Object> map = (Map<String, Object>) val;
                val = map.get(e);
            }else {
                val = SetGetUtils.invokeGetMethod(e, val);
            }
        }
        return val;
    }


    public static void wiredParamInStatement(SqlOutStatement statement, String column, Object value){
        if (value == null){
            statement.writeAftSeq(statement.getColumnName(column) + " is null ");
            return;
        }
        Class<?> type = value.getClass();
        if (Number.class.isAssignableFrom(type)){
            statement.writeEq(column, value.toString(), false);
        }else if (Boolean.class.equals(type) || boolean.class.equals(type)){
            statement.writeEq(column, value.toString(), false);
        }else if (Collection.class.isAssignableFrom(type)){
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            Collection<Object> collection = (Collection<Object>) value;
            for (Object obj : collection) {
                joiner.add(getString(obj));
            }
            List<String> stringArray = StreamUtils.mapList(collection, MapArgHandler::getString);
            statement.writeIn(column, false, stringArray);
        }else if (Map.class.isAssignableFrom(type)){
            throw new IllegalStateException("map value can not wired in single column");
        }else if (type.isArray()){
            Object[] array = (Object[]) value;
            ArrayList<Object> list = new ArrayList<>(Arrays.asList(array));
            wiredParamInStatement(statement, column, list);
        }else {
            statement.writeEq(column, getString(value), false);
        }


    }

    public static String getString(Object value){
        if (value == null){
            return "null";
        }
        Class<?> type = value.getClass();
        if (Number.class.isAssignableFrom(type)){
            return value.toString();
        }else if (Boolean.class.equals(type) || boolean.class.equals(type)){
            return value.toString();
        }else if (Collection.class.isAssignableFrom(type)){
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            Collection<Object> collection = (Collection<Object>) value;
            for (Object obj : collection) {
                joiner.add(getString(obj));
            }
            return joiner.toString();
        }else if (Map.class.isAssignableFrom(type)){
            StringJoiner joiner = new StringJoiner(" and ");
            Map<String, Object> map = (Map<String, Object>) value;
            for (String column : map.keySet()) {
                String sql = column + " = " + getString(map.get(column));
                joiner.add(sql);
            }
            return joiner.toString();
        }else if (type.isArray()){
            Object[] array = (Object[]) value;
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            for (Object o : array) {
                joiner.add(getString(o));
            }
            return joiner.toString();
        }

        return "'" + JdbcSqlUtils.getEscapeString(value.toString()) + "'";
    }



}
