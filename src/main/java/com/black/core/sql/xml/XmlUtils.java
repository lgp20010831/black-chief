package com.black.core.sql.xml;

import com.black.core.util.StringUtils;
import com.black.core.util.TextUtils;

import java.util.StringJoiner;

public class XmlUtils {


    public static String removeLineFeed(String sql){
        StringJoiner builder = new StringJoiner(" ");
        for (String s : sql.split("\n")) {
            builder.add(StringUtils.removeFrontSpace(s));
        }
        return builder.toString();
    }


    public static String prepareConditionItem(String item){
        item = item.replace("and", "&&");
        //替换 != null = notNull
        item = TextUtils.expressionReplacement(item, " notNull(", ") ", " != null", " !=null", " != Null");
        item = TextUtils.expressionReplacement(item, " isNull(", ") ", " ==null", "== null", "== Null");
        return item;
    }


    public static String compressSpaces(String str){
        StringBuilder builder = new StringBuilder();
        boolean spaceList = false;
        for (char c : str.toCharArray()) {
            if (c == ' '){
                if (!spaceList){
                    spaceList = true;
                    builder.append(c);
                }
            }else {
                spaceList = false;
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String compressSql(String sql){
        StringBuilder builder = new StringBuilder();
        boolean beforeBlank = false;
        for (char c : sql.toCharArray()) {
            if (c == '\n' || c == '\t'){
                if (!beforeBlank){
                    builder.append(" ");
                    beforeBlank = true;
                }
            }else {
                builder.append(c);
                beforeBlank = false;
            }
        }
        return builder.toString();
    }
}
