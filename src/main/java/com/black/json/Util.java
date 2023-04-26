package com.black.json;

import com.black.core.util.StringUtils;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    public static String getErrorTextTip(String text, int startIndex){
        return getErrorTextTip(text, startIndex, startIndex + 1);
    }

    public static String getErrorTextTip(String text, int startIndex, int endIndex){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        StringBuilder builder = new StringBuilder(text.substring(0, startIndex));
        //builder.append(AnsiOutput.toString(AnsiColor.RED, "$"));
        builder.append(AnsiOutput.toString(AnsiColor.BLUE, text.substring(startIndex, endIndex)));
        //builder.append(AnsiOutput.toString(AnsiColor.RED, "$"));
        builder.append(text.substring(endIndex));
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println(getErrorTextTip("{age:'18}", 5, 6));
    }

    public static Object deepAnalysisObject(Object obj){
        if (obj instanceof String){
            String string = obj.toString();
            if (isDouble(string)) {
                return Double.parseDouble(string);
            }

            if (isInt(string)){
                return Integer.parseInt(string);
            }

            if (isBool(string)){
                return Boolean.parseBoolean(string);
            }
            return string;
        }
        return obj;
    }

    public static boolean isBool(String str){
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }

    public static boolean isInt(String str){
        if (!StringUtils.hasText(str)){
            return false;
        }
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是不是double型
     * @param str
     * @return
     */
    public static boolean isDouble(String str){
        if (!StringUtils.hasText(str)){
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }


}
