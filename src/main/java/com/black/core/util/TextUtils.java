package com.black.core.util;

import com.black.core.spring.util.ApplicationUtil;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class TextUtils {

    //匹配格式  age is null and name is null    ** is null
    public static void match(String txt, String pattern){

    }

    public static String parseContent(String content, Object... params){
        StringBuilder builder = new StringBuilder();
        String[] sts = content.split("\\{}");
        String[] ps = new String[sts.length];
        for (int i = 0; i < ps.length; i++) {
            if (i >= params.length){
                ps[i] = "";
            }else {
                ps[i] = params[i] == null ? "null" : params[i].toString();
            }
        }
        for (int i = 0; i < sts.length; i++) {
            String str = sts[i];
            builder.append(str);
            builder.append(ps[i]);
        }
        return builder.toString();
    }

    public static String expressionReplacement(String text, String prefix, String suffix, String... sentences){
        StringJoiner joiner = new StringJoiner("|", "\\s+\\w*(", ")\\s");
        for (String sentence : sentences) {
            joiner.add(sentence);
        }
        Pattern pattern = Pattern.compile(joiner.toString());
        text = StringUtils.addIfNotStartWith(text, " ");
        text = StringUtils.addIfNotEndWith(text, " ");
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        int find = 0;
        int start = 0;
        int end = text.length();
        while (matcher.find()){
            find++;
            int t_start = matcher.start();
            int t_end = matcher.end();
            builder.append(text.substring(start, t_start));
            //拿到每一部分
            String part = text.substring(t_start, t_end);
            start = t_end;
            //处理 part
            int i = -1;
            for (String sentence : sentences) {
                i = part.indexOf(sentence);
                if (i != -1){
                    break;
                }
            }
            String local = part.substring(0, i);
            builder.append(prefix).append(local).append(suffix);
        }
        builder.append(text.substring(start, end));
        return builder.toString();
    }


    public static void main(String[] args) {

        String str = "hello xxx != null and ddd != null xxx";
//        Pattern compile = Pattern.compile("\\s+\\w*( != null| !=null)\\s");
//        Matcher matcher = compile.matcher(str);
//        int count = 0;
//        while (matcher.find()){
//            System.out.println("第" + (++count) + "次找到");
//            //start()返回上一个匹配项的起始索引
//            //end()返回上一个匹配项的末尾索引。
//            System.out.println(str.substring(matcher.start(),matcher.end()));
//        }

        ApplicationUtil.programRunMills(() -> {
            System.out.println(expressionReplacement(str, " notNull(", ") ", " != null", " !=null"));

        });

        String str2 = "name == null";
        ApplicationUtil.programRunMills(() ->{
            System.out.println(expressionReplacement(str, " isNill(", ") ",  " == null"));

        });

    }
}
