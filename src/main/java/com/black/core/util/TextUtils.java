package com.black.core.util;

public class TextUtils {


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


    public static void main(String[] args) {
        throw new ApplicationsException("hello: {}, name: {}", "lgp", 45);
    }
}
