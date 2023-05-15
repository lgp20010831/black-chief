package com.black.syntax;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StringUtils;
import com.black.throwable.ParserTxtException;
import com.black.utils.ServiceUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParseUtils {


    private enum FLAG{
        COMMON,
        OPEN_START,TOKEN, OPEN_ENDK
    }

    public static void main(String[] args) {
//        String str = "hello #{#{lgp#}} oo ${oooo} #{oisd}}";
//        for (int i = 0; i < 8; i++) {
//            str += str;
//        }
//        System.out.println(str.length());
//        doParse2(str);
//        String finalStr = str;
//        ApplicationUtil.programRunMills(() ->{
//            for (int i = 0; i < 2000; i++) {
//                String parse = doParse(finalStr);
//            }
//        });
//
//        ApplicationUtil.programRunMills(() ->{
//            for (int i = 0; i < 2000; i++) {
//                String parse = doParse2(finalStr);
//            }
//        });

        String txt = "ayc.setAge(15), ayc.setName('lgp'), ayc.setId(id), ayc.toString()";


    }
    static String doParse(String c){
        String r;
        //r = parse("#{", "}", c, String::toString);
        r = ServiceUtils.parseTxt(c, "#{", "}}", String::toString);
        return r;
    }

    static String doParse2(String c){
        String r;
        r = parse("#{", "}}", c, String::toString);
        //r = ServiceUtils.parseTxt(c, "#{", "}}", String::toString);
        return r;
    }

    public static String parse(@NonNull String start, @NonNull String end, String txt, @NonNull Function<String, String> function){
        if (!StringUtils.hasText(txt)){
            return txt;
        }

        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)){
            throw new IllegalStateException("start and end must has char");
        }
        txt = txt + " ";
        StringBuilder builder = new StringBuilder();
        StringBuilder tokenBuilder = new StringBuilder();
        FLAG flag = FLAG.COMMON;
        int length = txt.length();
        int match_start_index = 0;
        int match_end_index = 0;
        char[] startChars = start.toCharArray();
        char[] startCache = new char[startChars.length];
        char[] endChars = end.toCharArray();
        char[] endCache = new char[endChars.length];
        for (int i = 0; i < length; i++) {
            char c = txt.charAt(i);
            switch (flag){
                case COMMON:
                    if (startChars[0] == c) {
                        //如果匹配上了第一个字符
                        flag = FLAG.OPEN_START;
                        startCache[match_start_index++] = c;
                    }else {
                        builder.append(c);
                    }
                    break;
                case OPEN_START:
                    if (match_start_index == startChars.length){
                        //如果前缀全部匹配, 切换状态
                        flag = FLAG.TOKEN;
                        match_start_index = 0;
                        tokenBuilder.append(c);
                    }else {
                        if(startChars[match_start_index] == c){
                            //下一个字符又匹配上了
                            startCache[match_start_index++] = c;
                        }else {
                            //如果没有匹配上, 则将之前缓存的写入builder
                            writeTo(match_start_index, startCache, builder);
                            flag = FLAG.COMMON;
                        }
                    }
                    break;
                case TOKEN:
                    if (endChars[0] == c){
                        //token 读到尽头
                        flag = FLAG.OPEN_ENDK;
                        endCache[match_end_index++] = c;
                    }else {
                        tokenBuilder.append(c);
                    }
                    break;
                case OPEN_ENDK:
                    if (match_end_index == endChars.length){
                        //如果后缀全部匹配完
                        flag = FLAG.COMMON;
                        //处理 token
                        String token = tokenBuilder.toString();
                        String result = function.apply(token);
                        builder.append(result);
                        builder.append(c);
                        clearBuilder(tokenBuilder);
                        match_end_index = 0;
                    }else {
                        if(endChars[match_end_index] == c){
                            //下一个字符又匹配上了
                            endCache[match_end_index++] = c;
                        }else {
                            //如果没有匹配上, 则将之前缓存的写入builder
                            writeTo(match_end_index, endCache, tokenBuilder);
                            flag = FLAG.TOKEN;
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("unknown flag: " + flag);
            }
        }
        if (flag != FLAG.COMMON){

            switch (flag){
                case TOKEN:
                    writeTo(match_start_index, startCache, builder);
                    builder.append(tokenBuilder);
                    break;
                case OPEN_START:
                    writeTo(match_start_index, startCache, builder);
                    break;
                case OPEN_ENDK:
                    writeTo(match_start_index, startCache, builder);
                    builder.append(tokenBuilder);
                    if (match_end_index != endChars.length){
                        writeTo(match_end_index, endCache, builder);
                    }

            }
        }
        return builder.toString().substring(0, builder.length() - 1);
    }

    private static void clearBuilder(StringBuilder builder){
        builder.delete(0, builder.length());
    }

    private static void writeTo(int size, char[] chars, StringBuilder builder){
        for (int j = 0; j < size; j++) {
            builder.append(chars[j]);
        }
    }

    private enum METHOD_STATE{
        FIND_BEAN,
        ADD_METHOD_NAME,
        GET_PARAMS
    }

    //map.user.setAge(map.age), setName('lgp') E toString()
    //map.user.getAge()
    public static String parseMethod(String txt, Map<String, Object> environment, String notfindMethodValue){
        if (!StringUtils.hasText(notfindMethodValue)){
            notfindMethodValue = "";
        }
        METHOD_STATE state = METHOD_STATE.FIND_BEAN;
        Object currentBean = environment;
        StringBuilder beanNameBuilder = new StringBuilder();
        ArrayList<String> params = new ArrayList<>();
        StringBuilder paramNameBuilder = new StringBuilder();
        StringBuilder methodNameBuilder = new StringBuilder();
        String methodName = "";
        String currentMethodResult = notfindMethodValue;
        int length = txt.length();
        for (int i = 0; i < length; i++) {
            char c = txt.charAt(i);
            switch (state){
                case FIND_BEAN:
                    if (c == '.'){
                        Object bean = findBean(beanNameBuilder, currentBean);
                        if (bean == null){
                            return notfindMethodValue;
                        }
                        currentBean = bean;
                        clearBuilder(beanNameBuilder);
                    }else if (c == '('){
                        methodName = beanNameBuilder.toString();
                        clearBuilder(beanNameBuilder);
                        state = METHOD_STATE.GET_PARAMS;
                    }else {
                        beanNameBuilder.append(c);
                    }
                    break;
                case GET_PARAMS:
                    if (c == ','){
                        params.add(paramNameBuilder.toString());
                        clearBuilder(paramNameBuilder);
                    }else if (c == ')'){
                        if (paramNameBuilder.length() > 0) {
                            params.add(paramNameBuilder.toString());
                        }
                        Object[] args = new Object[params.size()];
                        for (int h = 0; h < params.size(); h++) {
                            args[h] = findValue(params.get(h), environment);
                        }
                        currentMethodResult = invokeMethod(methodName, currentBean, args, notfindMethodValue);
                        clearBuilder(paramNameBuilder);
                        params.clear();
                        state = METHOD_STATE.ADD_METHOD_NAME;
                    }else {
                        paramNameBuilder.append(c);
                    }
                    break;
                case ADD_METHOD_NAME:
                    if(c == ' '){

                    }else if (c == ','){
                        clearBuilder(methodNameBuilder);
                    }else if (c == '('){
                        methodName = methodNameBuilder.toString();
                        clearBuilder(methodNameBuilder);
                        state = METHOD_STATE.GET_PARAMS;
                    }else if (c == '.'){
                        Object bean = findBean(methodNameBuilder, environment);
                        if (bean == null){
                            return notfindMethodValue;
                        }
                        currentBean = bean;
                        clearBuilder(methodNameBuilder);
                        state = METHOD_STATE.FIND_BEAN;
                    }else {
                        methodNameBuilder.append(c);
                    }
                    break;
            }
        }

        switch (state){
            case FIND_BEAN:
                throw new ParserTxtException("method declaration does not exist in statement");
            case GET_PARAMS:
                throw new ParserTxtException("method parameter declaration is not closed");
        }
        return currentMethodResult;
    }

    public static String invokeMethod(String methodName, Object bean, Object[] args, String notfindMethodValue){
        ClassWrapper<Object> classWrapper = ClassWrapper.get(BeanUtil.getPrimordialClass(bean));
        MethodWrapper method = classWrapper.getMethod(methodName, args.length);
        if (method == null){
            return notfindMethodValue;
        }
        Object result = method.invoke(bean, args);
        return result == null ? "null" : result.toString();
    }

    private static Object findBean(StringBuilder builder, Object currentBean){
        String beanName = builder.toString();
        if (currentBean == null){
            return null;
        }
        if (currentBean instanceof Map){
            currentBean = ((Map<?, ?>) currentBean).get(beanName);
        }else {
            currentBean = SetGetUtils.invokeGetMethod(beanName, currentBean);
        }
        return currentBean;
    }

    private static Object findValue(String txt, Map<String, Object> environment){
        if (!StringUtils.hasText(txt)){
            return null;
        }

        if (isNumeric(txt)){
            return Integer.parseInt(txt);
        }

        if ("false".equalsIgnoreCase(txt)){
            return Boolean.FALSE;
        }

        if ("true".equalsIgnoreCase(txt)){
            return Boolean.TRUE;
        }

        if (txt.startsWith("'") && txt.endsWith("'") && txt.length() > 2){
            return txt.substring(1 , txt.length() - 1);
        }

        return ServiceUtils.findValue(environment, txt);
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
