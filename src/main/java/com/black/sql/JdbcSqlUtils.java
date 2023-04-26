package com.black.sql;

import com.black.core.util.StringUtils;

public class JdbcSqlUtils {


    public static String getEscapeString(String sql){
        if (!StringUtils.hasText(sql)){
            return sql;
        }
        StringBuilder builder = new StringBuilder();
        int length = sql.length();
        for (int i = 0; i < length; i++) {
            char c = sql.charAt(i);
            if (c == '\''){
                builder.append("'");
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String getString(String str, boolean stringType, boolean autoEscape){
        String escapeString = autoEscape ? getEscapeString(str) : str;
        return stringType ? "'" + escapeString + "'" : escapeString;
    }

}
