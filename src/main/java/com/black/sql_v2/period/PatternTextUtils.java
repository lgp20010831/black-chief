package com.black.sql_v2.period;

import com.black.core.util.StringUtils;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author 李桂鹏
 * @create 2023-05-12 11:56
 */
@SuppressWarnings("all")
public class PatternTextUtils {

    public static String addFlag(String str, int start, int end, String prefix, String suffix){
        StringBuilder builder = new StringBuilder();
        builder.append(str.substring(0, start));
        builder.append(prefix);
        builder.append(str.substring(start, end));
        builder.append(suffix);
        builder.append(str.substring(end));
        return builder.toString();
    }

    public static String parse(@NonNull String start, @NonNull String end, String txt, @NonNull IndexParseTextFunction function){
        if (!StringUtils.hasText(txt)){
            return txt;
        }

        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)){
            throw new IllegalStateException("start and end must has char");
        }
        StringBuilder builder = new StringBuilder();
        StringBuilder tokenBuilder = new StringBuilder();
        FLAG flag = FLAG.COMMON;
        int length = txt.length();
        int match_start_index = 0;
        int match_end_index = 0;
        int textStartIndex = 0;
        int textEndIndex = 0;
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
                        textStartIndex = i;
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
                            textStartIndex = -1;
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
                        textEndIndex = i;
                        //如果后缀全部匹配完
                        flag = FLAG.COMMON;
                        //处理 token
                        String token = tokenBuilder.toString();
                        String result = function.parse(token, textStartIndex, textEndIndex);
                        builder.append(result);
                        builder.append(c);
                        clearBuilder(tokenBuilder);
                        match_end_index = 0;
                        textStartIndex = -1;
                        textEndIndex = -1;
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
        return builder.toString();
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

    private enum FLAG{
        COMMON,
        OPEN_START,TOKEN, OPEN_ENDK
    }

    public static boolean isCharacter(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    @Data
    public static class OperatorPack{
        private final String operator;

        private final int start;

        private final int end;

        private int index;

        public OperatorPack(String operator, int start, int end) {
            this.operator = operator;
            this.start = start;
            this.end = end;
        }
    }

    public static OperatorPack checkOperator(String txt, int si){
        //0 为找到符号, 1拼凑符号中
        int state = 0;
        int fhStart = 0;
        int fhEnd = 0;
        boolean characterConnect = false;
        StringBuilder fh = new StringBuilder();
        while (si != -1){
            si--;
            char at = txt.charAt(si);
            if (state == 0){
                if (at == ' '){
                    continue;
                }
                if (at == '=' || at == '!'){
                    state = 1;
                    fhEnd = si;
                    fh.append(at);
                }

                if (PatternTextUtils.isCharacter(at)){
                    state = 1; fhEnd = si; characterConnect = true; fh.append(at);
                }
            }

            else if (state == 1){
                if (at == ' '){
                    fhStart = si + 1;
                    break;
                }

                if (PatternTextUtils.isCharacter(at) && !characterConnect){
                    break;
                }
                fh.append(at);
            }

        }

        StringBuilder builder = new StringBuilder();
        for (int i = fh.length() - 1; i >= 0; i--) {
            builder.append(fh.charAt(i));
        }
        return new OperatorPack(builder.toString(), fhStart, fhEnd);
    }
}
