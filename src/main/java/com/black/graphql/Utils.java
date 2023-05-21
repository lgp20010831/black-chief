package com.black.graphql;


import com.black.core.util.StringUtils;
import com.black.graphql.core.request.result.ResultAttributtes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    public static void main(String[] args) {
        ResultAttributtes[] attributes = parseResultAttributes("result[name, age, lin[1, 2, 3[4, l]]], message[cnm], successful[name, hello], code");
        System.out.println(Arrays.toString(attributes));
    }

    static Map<String, ResultAttributtes[]> cache = new ConcurrentHashMap<>();

    public static Map<String, String> parseHeader(String[] txts){
        Map<String, String> map = new HashMap<>();
        for (String txt : txts) {
            String[] split = StringUtils.split(txt, "=", 2, "异常 header 格式: " + txt);
            map.put(split[0].trim(), split[1].trim());
        }
        return map;
    }

    public static ResultAttributtes[] parseResultAttributes(String txt){
        return cache.computeIfAbsent(txt, tx ->{
            if (!StringUtils.hasText(txt)){
                return null;
            }

            return doParse(txt);
        });
    }

    private static ResultAttributtes[] doParse(String context){
        int start = context.indexOf("[");
        if (start == -1){
            return parseNoArray(context);
        }

        List<ResultAttributtes> attributtes = new ArrayList<>();
        StringBuilder name = new StringBuilder();
        ResultAttributtes[] fathers = new ResultAttributtes[25];
        int step = 0;
        for (char chr : context.toCharArray()) {

            if (chr == ' ') continue;

            if (chr == ',' ){
                if (name.length() > 0){
                    if (step == 0){
                        attributtes.add(new ResultAttributtes(name.toString()));
                    }else {
                        fathers[step - 1].addResultAttributes(new ResultAttributtes(name.toString()));
                    }
                    name = new StringBuilder();
                }
            }
            else
            if (chr == '['){
                ResultAttributtes ra = new ResultAttributtes(name.toString());
                fathers[step ++] = ra;
                if (step != 1){
                    fathers[step - 2].addResultAttributes(ra);
                }
                name = new StringBuilder();
            }
            else
            if (chr == ']'){
                if (name.length() > 0){
                    fathers[step - 1].addResultAttributes(new ResultAttributtes(name.toString()));
                }
                if (step - 1 == 0){
                    attributtes.add(fathers[step - 1]);
                }
                fathers[-- step] = null;
                name = new StringBuilder();
            }
            else {
                name.append(chr);
            }
        }

        return attributtes.toArray(new ResultAttributtes[0]);
    }


    public static int longestValidParentheses(String s) {
        int w = 0;
        char[] fathers = new char[25];
        int step = 0;
        int son = 0;
        for (char chr : s.toCharArray()) {

            if (chr == '('){
                fathers[step ++] = chr;
            }
            else
            if (chr == ')'){

                if (step == 0){
                    continue;
                }
                fathers[-- step] = 0;
                son += 2;
                if (step == 0){
                    w += son;
                    son = 0;
                }
            }
        }
        return w;
    }


    private static ResultAttributtes[] parseNoArray(String txt){
        String[] entry = txt.split(",");
        ResultAttributtes[] attributtes = new ResultAttributtes[entry.length];
        for (int i = 0; i < entry.length; i++) {
            attributtes[i] = new ResultAttributtes(entry[i]);
        }
        return attributtes;
    }


    public static boolean isQuery(String name){
        return name.startsWith("select") ||
                name.startsWith("get") ||
                name.startsWith("query") ||
                name.startsWith("find") ||
                name.startsWith("obtain") ||
                name.startsWith("read") ||
                name.startsWith("inquiry") ||
                name.startsWith("list") ||
                name.startsWith("map");
    }
}
