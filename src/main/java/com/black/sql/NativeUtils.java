package com.black.sql;

public class NativeUtils {

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

    public static void clearBuilder(StringBuilder builder){
        if (builder != null){
            builder.delete(0, builder.length());
        }
    }

}
