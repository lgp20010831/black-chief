package com.black.xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.util.StringUtils;

@SuppressWarnings("all")
public class SyntaxParseUtils {



    public static void parseParam(String syntax){
        boolean required = syntax.startsWith("!");
        syntax = StringUtils.removeIfStartWith(syntax, "!");
        boolean body = syntax.startsWith("?");
        syntax = StringUtils.removeIfStartWith(syntax, "?");
        String[] nameAndType = syntax.split("::");

        Class<?> type = String.class;
        if (nameAndType.length == 2){
            type = Object.class;
        }
        String name = nameAndType[0];
        if (body){
            if (!type.equals(JSONObject.class) && !type.equals(JSONArray.class)){
                type = JSONObject.class;
            }
        }

        System.out.println(StringUtils.joinStringWithDel("-", required, body, type, name));
    }


    public static void main(String[] args) {
        parseParam("!id::int");
    }
}
