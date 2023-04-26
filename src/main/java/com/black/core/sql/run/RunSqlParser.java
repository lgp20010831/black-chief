package com.black.core.sql.run;

import com.black.aviator.AviatorManager;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.util.Av0;
import com.black.core.util.SetGetUtils;
import com.black.sql.JdbcSqlUtils;
import com.black.sql.NativeUtils;
import com.black.utils.ServiceUtils;

import java.util.Map;

public class RunSqlParser {

    public static final String START = "#{";

    public static final String END = "}";


    public static void main(String[] args) {
        String  sql = "select * from ayc where 1 = 1 ?[isNotNull(name) ? and name::varchar like '%#{name}%' : and name is null]";
        Map<String, Object> objectMap = Av0.js("name", "lgp");
        sql = ServiceUtils.parseTxt(sql, "?[", "]", context -> {
            int wi = find(context, "?");
            if (wi == -1)
                throw new IllegalStateException("?[] 应具有 ? 表达式");
            int di = find(context, ":");
            String trueContext;
            String falseContext = "";
            if (di == -1){
                trueContext = context.substring(wi + 1);
            }else if (di > wi){
                trueContext = context.substring(wi + 1, di);
                falseContext = context.substring(di + 1);
            }else {
                throw new IllegalStateException("di: " + di + " but wi:" + wi);
            }
            String boolContext = context.substring(0, wi);
            Boolean res = (Boolean) AviatorManager.getInstance().execute(boolContext, objectMap);
            return res ? trueContext : falseContext;
        });
        System.out.println(sql);

//        String sql = "::date ... : xxxx";
//        System.out.println(find(sql, ":"));
    }

    //select * from ayc ?{isNull(name) ? name = %<map.name> : }}
    public static String parseSql(String sql, MethodWrapper mw, Object[] args){
        if (sql == null) return sql;
        Map<String, Object> objectMap = MapArgHandler.parse(args, mw);
        return parseSql(sql, objectMap);
    }

    public static String parseSql(String sql, Map<String, Object> objectMap){
        if (sql == null) return sql;
        sql = ServiceUtils.parseTxt(sql, "?[", "]", context -> {
            int wi = find(context, "?");
            if (wi == -1)
                throw new IllegalStateException("?[] 应具有 ? 表达式");
            int di = find(context, ":");
            String trueContext;
            String falseContext = "";
            if (di == -1){
                trueContext = context.substring(wi + 1);
            }else if (di > wi){
                trueContext = context.substring(wi + 1, di);
                falseContext = context.substring(di + 1);
            }else {
                throw new IllegalStateException("di: " + di + " but wi:" + wi);
            }
            String boolContext = context.substring(0, wi);
            Boolean res = (Boolean) AviatorManager.getInstance().execute(boolContext, objectMap);
            return res ? trueContext : falseContext;
        });
        sql = ServiceUtils.parseTxt(sql, START, END, key -> {
            Object val = ServiceUtils.findValue(objectMap, key);
            return MapArgHandler.getString(val);
        });

        sql = ServiceUtils.parseTxt(sql, "^{", "}", key -> {
            Object value = ServiceUtils.findValue(objectMap, key);
            return value == null ? "null" : JdbcSqlUtils.getEscapeString(value.toString());
        });
        return sql;
    }

    public static int find(String context, String indexStr){
        int wi = context.indexOf(indexStr);
        while (wi != -1){
            if (NativeUtils.isLegitimate(context, wi) && isIndie(context, wi)){
                break;
            }
            if (wi == context.length() - 1)
                return -1;
            wi = context.indexOf(indexStr, wi + 1);
        }
        return wi;
    }

    public static boolean isIndie(String context, int target){
        int index = target;
        if (index == -1) return false;
        if (index != 0){
            if (!(context.charAt(index - 1) == ' ')) {
                return false;
            }
        }
        if (index != context.length() - 1){
            if (!(context.charAt(index + 1) == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static Object getValue(Map<String, ParameterWrapper> parameterWrappers, Object[] args, String entry){
        if (entry == null) return null;
        Object val = parameterWrappers;
        for (String e : entry.split("\\.")) {
            if (val == null) return null;
            if (val instanceof Map){
                Map<String, Object> map = (Map<String, Object>) val;
                val = map.get(e);
                if (val instanceof ParameterWrapper){
                    ParameterWrapper pw = (ParameterWrapper) val;
                    val = args[pw.getIndex()];
                }
            }else {
                val = SetGetUtils.invokeGetMethod(e, val);
            }
        }
        return val;
    }

}
