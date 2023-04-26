package com.black.db;

import com.black.aviator.AviatorManager;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.sql.NativeUtils;
import com.black.utils.ServiceUtils;
import lombok.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtils {

    public static List<String> parseGeneratedKeys(ResultSet resultSet){
        List<String> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        }catch (SQLException e){
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeResultSet(resultSet);
        }
        return list;
    }

    public static Map<Object, Object> castParamArray(Object... params){
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            map.put(i + 1, params[i]);
        }
        return map;
    }

    public static boolean idioticJudgeIsSelectSql(@NonNull String sql){
        sql = StringUtils.removeFrontSpace(sql);
        return StringUtils.startsWithIgnoreCase(sql, "select");
    }

    public static String parseDichotomousSql(String sql, Map<String, Object> objectMap){
        return ServiceUtils.parseTxt(sql, "?[", "]", context -> {
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
    
}
