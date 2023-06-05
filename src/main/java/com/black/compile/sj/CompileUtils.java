package com.black.compile.sj;

import com.black.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-05 17:20
 */
@SuppressWarnings("all")
public class CompileUtils {


    public static String[] spilts(String str, char f){
        if (!StringUtils.hasText(str)){
            return new String[0];
        }

        if (str.indexOf(f) == -1){
            return new String[]{str};
        }
        List<String> spiltList = new ArrayList<>();
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean clude = false;
        for (char c : chars) {
            if (c == '\'' || c == '\"'){
                clude = !clude;
            }

            if (c == f && !clude){
                spiltList.add(builder.toString());
                clearBuilder(builder);
            }else {
                builder.append(c);
            }
        }
        if (builder.length() > 0){
            spiltList.add(builder.toString());
        }
        return spiltList.toArray(new String[0]);
    }

    public static void clearBuilder(StringBuilder builder){
        builder.delete(0, builder.length());
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(spilts("string str = 'hello world;'; int i =3;", ';')));
    }
}
