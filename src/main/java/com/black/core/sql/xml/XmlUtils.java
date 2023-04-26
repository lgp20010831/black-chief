package com.black.core.sql.xml;

import com.black.core.util.StringUtils;

import java.util.StringJoiner;

public class XmlUtils {


    public static String removeLineFeed(String sql){
        StringJoiner builder = new StringJoiner(" ");
        for (String s : sql.split("\n")) {
            builder.add(StringUtils.removeFrontSpace(s));
        }
        return builder.toString();
    }


}
