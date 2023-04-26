package com.black.syntax;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.util.SetGetUtils;
import com.black.throwable.ParserTxtException;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class SyntaxUtils {


    public static String parseQuestionMarkSyntax(String txt, Object... params){
        Map<Object, Object> indexParamMap = indexParamMap(params);
        return parseQuestionMarkSyntaxByMap(txt, indexParamMap);
    }

    public static String parseQuestionMarkSyntaxByMap(String txt, Map<Object, Object> indexParamMap){
        StringBuilder builder = new StringBuilder();
        char[] charArray = txt.toCharArray();
        boolean jump = false;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '?'){
                if (isLegitimate(txt, i)) {
                    jump = true;
                    builder.append(wiredParam(txt, i, indexParamMap));
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

    public static Map<Object, Object> indexParamMap(Object... params){
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < params.length; i++) {
            map.put(i + 1, params[i]);
        }
        return map;
    }

    public static String wiredParam(String sql, int index, Map<Object, Object> paramMap){
        char paramIndexChar;
        if (index + 1 >= sql.length() || !Character.isDigit(paramIndexChar = sql.charAt(index + 1))){
            throw new ParserTxtException("sql ?后面需要指定参数下标, sql: " + sql + " 异常点: " + index);
        }
        int paramIndex = Integer.parseInt(String.valueOf(paramIndexChar));
        if (!paramMap.containsKey(paramIndex)) {
            throw new ParserTxtException("缺少参数下标定义: " + paramIndex + ", sql: " + sql);
        }
        return MapArgHandler.getString(paramMap.get(paramIndex));
    }

    public static boolean isLegitimate(String txt,  int index){
        String prefix = txt.substring(0, index);

        int i1 = multivariate(prefix, '\'');
        if (isOdd(i1)){
            return false;
        }
        int i2 = multivariate(prefix, '"');
        return !isOdd(i2);
    }

    public static int multivariate(String txt, char sel){
        int count = 0;
        for (char c : txt.toCharArray()) {
            if (c == sel){
                count++;
            }
        }
        return count;
    }

    public static boolean isOdd(int x){
        return (x & 1) != 0;
    }

    //param.name  {param:(name, age)}
    public static SyntaxMetadata findValue(@NonNull String expression, Map<String, Object> source){
        SyntaxMetadata metadata = new SyntaxMetadata();
        metadata.setPath(expression);
        metadata.setEnvironment(source);
        Object parent;
        String name = null;
        Object val = source;
        FieldWrapper fw = null;
        boolean currentGetFromField = false;
        String[] split = expression.split("\\.");
        if (split.length > 0){
            for (String e : split) {
                name = e;
                parent = val;
                try {
                    if (val == null){
                        break;
                    }
                    if (val instanceof Map){
                        Map<String, Object> map = (Map<String, Object>) val;
                        val = map.get(e);
                        currentGetFromField = false;
                    }else {
                        ClassWrapper<?> cw = ClassWrapper.get(val.getClass());
                        fw = cw.getField(e);
                        val = SetGetUtils.invokeGetMethod(e, val);
                        currentGetFromField = true;
                    }
                }finally {
                    metadata.setParent(parent);
                }
            }
        }else {
            val = null;
        }
        metadata.setField(currentGetFromField);
        metadata.setFieldMetadata(fw);
        metadata.setName(name);
        metadata.setValue(val);
        return metadata;
    }


}
