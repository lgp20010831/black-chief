package com.black.core.sql.code.mapping;


import com.black.syntax.SyntaxMethodContextParser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//注册映射的几种方式
/*
    1.配置文件 mapping: 下
    2.收集实现了接口的类
    3.收集包含了指定注解的 class 读取注解上的值
 */
public class GlobalMapping {

    private static final Map<String, String> mappingMap = new ConcurrentHashMap<>();

    private static final Map<String, String> parseCache = new ConcurrentHashMap<>();

    public static final String START = "${";

    public static final String END = "}";

    public static Map<String, String> getParseCache() {
        return parseCache;
    }

    public static Map<String, String> getMappingMap() {
        return mappingMap;
    }

    public static String parseAndObtain(String txt){
        return parseAndObtain(txt, false);
    }

    public static String parseAndObtain(String txt, boolean useChae){
        return parseAndObtain(txt, START, END, useChae);
    }
    public static String dynamicParse(String txt, Map<String, Object> source){
        return dynamicParse(txt, START, END, source);
    }

    public static String dynamicParse(String txt, String start, String end, Map<String, Object> source){
        Map<String, Object> map = new HashMap<>();
        if (source != null){
            map.putAll(source);
        }

        StringBuilder builder = new StringBuilder();
        String machingStr = txt;
        //首先判断参数填充
        //http:#{url}/#{mapping}/#{a}?value=#{value}&age=#{age}
        int processor = 0;
        int i = machingStr.indexOf(start);
        int lastEndIndex = 0;
        //update ayc set ${u}, name = #{source.name} where ${d} and id = #{source.id}
        while (i != -1){
            for (;;){
                lastEndIndex = machingStr.indexOf(end);
                if (lastEndIndex > i) break;
                else {
                    int fi = lastEndIndex + end.length();
                    builder.append(machingStr, 0, fi);
                    machingStr = machingStr.substring(fi);
                    i = machingStr.indexOf(start);
                    processor = processor + fi;
                }
            }
            if (lastEndIndex == -1){
                throw new MappingException("Missing Terminator: " + end);
            }

            if (i != 0){
                builder.append(machingStr, 0, i);
            }

            //dict()
            String key = machingStr.substring(i + start.length(), lastEndIndex);
            String parseTxt = SyntaxMethodContextParser.parseTxt(key, map);
            builder.append(parseTxt);
            int si = lastEndIndex + end.length();
            machingStr = machingStr.substring(si);
            processor = processor + si;
            i = machingStr.indexOf(start);
        }

        if (lastEndIndex != -1 && lastEndIndex != txt.length() - 1){
            builder.append(txt.substring(processor));
        }
        String afterParse = builder.toString();
        return afterParse;
    }

    public static String parseAndObtain(String txt, String start, String end, boolean useCache){

        if (parseCache.containsKey(txt)){
            return parseCache.get(txt);
        }
        StringBuilder builder = new StringBuilder();
        String machingStr = txt;
        //首先判断参数填充
        //http:#{url}/#{mapping}/#{a}?value=#{value}&age=#{age}
        int processor = 0;
        int i = machingStr.indexOf(start);
        int lastEndIndex = 0;
        //update ayc set ${u}, name = #{source.name} where ${d} and id = #{source.id}
        while (i != -1){
            for (;;){
                lastEndIndex = machingStr.indexOf(end);
                if (lastEndIndex > i) break;
                else {
                    int fi = lastEndIndex + end.length();
                    builder.append(machingStr, 0, fi);
                    machingStr = machingStr.substring(fi);
                    i = machingStr.indexOf(start);
                    processor = processor + fi;
                }
            }
            if (lastEndIndex == -1){
                throw new MappingException("Missing Terminator: " + end);
            }

            if (i != 0){
                builder.append(machingStr, 0, i);
            }

            String key = machingStr.substring(i + start.length(), lastEndIndex);

            String v = obtain(key);
            builder.append(v);
            int si = lastEndIndex + end.length();
            machingStr = machingStr.substring(si);
            processor = processor + si;
            i = machingStr.indexOf(start);
        }

        if (lastEndIndex != -1 && lastEndIndex != txt.length() - 1){
            builder.append(txt.substring(processor));
        }
        String afterParse = builder.toString();
        if (useCache){
            parseCache.put(txt, afterParse);
        }
        return afterParse;
    }


    public static String obtain(String primary){
        if (primary == null) return null;
        if (mappingMap.containsKey(primary)){
            return mappingMap.get(primary);
        }
        return primary;
    }

    public static void registerMapping(String primary, String mapping){
        if (primary != null && mapping != null){
            mappingMap.put(primary, mapping);
        }
    }

    public static void main(String[] args) {
        registerMapping("dict", "d.name \"#{arg1}\"|left join dict d on d.p_code = '#{arg2}' and d.code = r.#{arg3}|d.id is not null");
        String parse = dynamicParse("${dict(typeName, ZZZT, project_type)}", null);


        System.out.println(parse);

    }
}
