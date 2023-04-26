package com.black.core.util;

import org.springframework.lang.Nullable;

import java.util.StringJoiner;

public class StringUtils {

    //判断这个字符串片段在 整个文本里是不是独立的   isIndependent("abc", "b") = false
    //isIndependent(abc b, b) = true
    //isIndependent(a b c, b) = true
    public static boolean isIndependent(String txt, String seq){
        int tl = txt.length();
        int sl = seq.length();
        if (tl < sl)
            return false;
        int i = txt.indexOf(seq);
        if (i == -1)
            return false;
        if (tl == sl)
            return true;

        while (i != -1){
            int pre = i - 1;
            int aft = i + sl;
            boolean preInd = true;
            boolean aftInd = true;
            if (pre >= 0 && pre < tl){
                char preChar = txt.charAt(pre);
                preInd = preChar == ' ';
            }

            if (aft >= 0 && aft < tl){
                char aftChar = txt.charAt(aft);
                aftInd = aftChar == ' ';
            }

            if (preInd && aftInd)
                return true;

            i = txt.indexOf(seq, i + sl);
        }
        return false;
    }

    public static boolean isBlank(String txt){
        return !hasText(txt);
    }

    public static void main(String[] args) {
        System.out.println(startsWithIgnoreCase("SELECT * from",  "select"));
    }

    public static boolean startsWithIgnoreCase(String searchIn, String searchFor){
        return startsWithIgnoreCase(searchIn, 0, searchFor);
    }

    public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
        return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
    }

    private static boolean isCharAtPosNotEqualIgnoreCase(String searchIn, int pos, char firstCharOfSearchForUc, char firstCharOfSearchForLc) {
        return Character.toLowerCase(searchIn.charAt(pos)) != firstCharOfSearchForLc && Character.toUpperCase(searchIn.charAt(pos)) != firstCharOfSearchForUc;
    }

    /**
     * Finds the position of a substring within a string ignoring case.
     *
     * @param searchIn
     *            the string to search in
     * @param searchFor
     *            the array of strings to search for
     * @return the position where <code>searchFor</code> is found within <code>searchIn</code> starting from <code>startingPosition</code>.
     */
    public static int indexOfIgnoreCase(String searchIn, String searchFor) {
        return indexOfIgnoreCase(0, searchIn, searchFor);
    }

    /**
     * Finds the position of a substring within a string ignoring case.
     *
     * @param startingPosition
     *            the position to start the search from
     * @param searchIn
     *            the string to search in
     * @param searchFor
     *            the array of strings to search for
     * @return the position where <code>searchFor</code> is found within <code>searchIn</code> starting from <code>startingPosition</code>.
     */
    public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
        if ((searchIn == null) || (searchFor == null)) {
            return -1;
        }

        int searchInLength = searchIn.length();
        int searchForLength = searchFor.length();
        int stopSearchingAt = searchInLength - searchForLength;

        if (startingPosition > stopSearchingAt || searchForLength == 0) {
            return -1;
        }

        // Some locales don't follow upper-case rule, so need to check both
        char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
        char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));

        for (int i = startingPosition; i <= stopSearchingAt; i++) {
            if (isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                // find the first occurrence of the first character of searchFor in searchIn
                while (++i <= stopSearchingAt && (isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc))) {
                }
            }

            if (i <= stopSearchingAt && startsWithIgnoreCase(searchIn, i, searchFor)) {
                return i;
            }
        }

        return -1;
    }

    public static String unruacnl(String name){
        char[] chars = name.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z'){
                builder.append("_");
                char i = (char) (c + 32);
                builder.append(i);
            }else {
                builder.append(c);
            }
        }

        String str = builder.toString();
        str = StringUtils.removeIfStartWith(str, "_");
        return str;
    }

    public static String ruacnl(String str){
        return ruacnl(str, '_');
    }

    public static String ruacnl(String str, char ch){
        if (str.indexOf(ch) == -1){
            return str;
        }
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean d = false;
        for (char c : chars) {
            if (c == ch){
                d = true;
            }else {
                if (d){
                    builder.append(Character.toUpperCase(c));
                    d = false;
                }else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }


    public static String removeTrailingSpace(String s){
        if (s == null) return null;
        for (;;){
            if (s.endsWith(" ")){
                s = removeLast(s);
                continue;
            }
            break;
        }
        return s;
    }

    public static boolean expectSplit(String str, String sp, int expect){
        if (str == null || sp == null)
            return false;
        return str.split(sp).length == expect;
    }


    public static String addIfEndWith(String str, String seq){
        if (str.endsWith(seq)){
            return str.concat(seq);
        }
        return str;
    }

    public static String addIfStartWith(String str, String seq){
        if (str.startsWith(seq)){
            return seq.concat(str);
        }
        return str;
    }

    public static String addIfNotEndWith(String str, String seq){
        if (!str.endsWith(seq)){
            return str.concat(seq);
        }
        return str;
    }

    public static String addIfNotStartWith(String str, String seq){
        if (!str.startsWith(seq)){
            return seq.concat(str);
        }
        return str;
    }

    public static String removeFrontSpace(String s){
        if (s == null) return null;
        for (;;){
            if (s.startsWith(" ")){
                s = removefirst(s);
                continue;
            }
            break;
        }
        return s;
    }

    public static String removeLines(String s){
        if (s == null) return null;
        for (;;){
            if (s.startsWith(" ") || s.startsWith("\n")){
                s = removefirst(s);
                continue;
            }
            break;
        }
        return s;
    }

    public static String removeIfEndWith(String txt, String seq){
        if (seq == null || txt == null) return txt;
        if (txt.endsWith(seq)){
            txt = txt.substring(0, txt.lastIndexOf(seq));
        }
        return txt;
    }

//    public static String removeIfEndWithIgnoreCase(String txt, String seq){
//        if (seq == null || txt == null) return txt;
//        if (txt.endsWith(seq)){
//            txt = txt.substring(0, txt.lastIndexOf(seq));
//        }
//        return txt;
//    }

    public static String removeIfEndWiths(String srt, String... seqs){
        if (!hasText(srt)){
            return srt;
        }
        for (String seq : seqs) {
            if (srt.endsWith(seq)){
                return srt.substring(0, srt.lastIndexOf(seq));
            }
        }
        return srt;
    }

    public static boolean endWiths(String txt, String... strs){
        if (txt == null){
            return false;
        }
        for (String str : strs) {
            if (txt.endsWith(str)){
                return true;
            }
        }
        return false;
    }

    public static boolean startWiths(String txt, String... strs){
        if (txt == null){
            return false;
        }
        for (String str : strs) {
            if (txt.startsWith(str)){
                return true;
            }
        }
        return false;
    }

    public static String removeIfStartWiths(String srt, String... seqs){
        if (!hasText(srt)){
            return srt;
        }
        for (String seq : seqs) {
            if (srt.startsWith(seq)){
                return srt.substring(seq.length());
            }
        }
        return srt;
    }

    public static String removeIfStartWithsIgnoreCase(String srt, String... seqs){
        if (!hasText(srt)){
            return srt;
        }
        for (String seq : seqs) {
            if (startsWithIgnoreCase(srt, seq)){
                return srt.substring(seq.length());
            }
        }
        return srt;
    }

    public static String removeIfStartWith(String srt, String seq){
        if (seq == null || srt == null) return srt;
        if (srt.startsWith(seq)){
            return srt.substring(seq.length());
        }
        return srt;
    }

    public static String removeIfStartWithIgnoreCase(String srt, String seq){
        if (seq == null || srt == null) return srt;
        if (startsWithIgnoreCase(srt, seq)){
            return srt.substring(seq.length());
        }
        return srt;
    }

    public static String[] split(String str, String lit){
        if (str == null) return new String[0];
        return str.split(lit);
    }

    public static String[] split(String str, String lit, int expect, String message){
        if (str == null) throw new NullPointerException("str is null");
        String[] split = str.split(lit);
        if (split.length != expect)
            throw new IllegalStateException(message);
        return split;
    }

    public static String notNull(String txt){
        return txt == null ? "" : txt;
    }

    public static String removeLast(String str){
        if (str == null) return null;
        return str.substring(0, str.length() - 1);
    }


    public static String removefirst(String str){
        if (str == null) return null;
        return str.substring(1);
    }

    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String ifStartWithDotOrUnderlineDoRemove(String str){
        if (str.startsWith(".") || str.startsWith("_"))
            str = str.substring(1);

        return str;
    }

    public static String removeNumbers(String str){
        return str.replaceAll("\\d+","");
    }

    public static String removeLastDot(String str){

        if (str.endsWith(".")){
            return str.substring(0,str.length()-1);
        }
        return str;
    }

    /** 去掉文件的后缀 */
    public static String removeFileTypeSuffix(String str){
        int index = str.lastIndexOf(".");

        if (index == -1)
            return str;

        return str.substring(0,index);
    }

    public static String removeUnder(String str){
        return linkStr(str.split("_"));
    }

    /** 去除下划线，然后将下划线下一个字母大写 */
    public static String removeUnderscoresAndCapitalizeNextLetter(String str){
        String[] strs = str.split("_");

        String finStr = "";

        for (String string : strs) {
            finStr = finStr.concat(titleCase(string));
        }

        return finStr;
    }

    public static String capitalCharacterBreak(String str){

        String newStr = "";
        char[] chars;
        for (int i = 0; i < (chars = str.toCharArray()).length; i++) {

            char molecule = chars[i];
            String node;
            if (molecule >= 'A' && molecule <= 'Z')
                node = concatStr("_", String.valueOf(molecule));
            else
                node = String.valueOf(molecule).toUpperCase();

            newStr = concatStr(newStr, node);
        }

        return newStr;
    }


    /**  拼接字符串 */
    public static String concatStr(String... strs) {
        String finStr = "";
        for (String str : strs) {

            if (str == null)continue;

            finStr = finStr.concat(str);
        }

        return finStr;
    }

    public static String getString(Object value){
        return getString(value, null);
    }

    public static String getString(Object value, String defaultValue){
        if (value == null){
            return defaultValue;
        }
        return value.toString();
    }

    public static String joinString(Object... array){
        return joinStringWithDel("", "", "", array);
    }

    public static String joinStringWithDel(CharSequence delimiter, Object... array){
        return joinStringWithComplete(delimiter, "", "", array);
    }


    public static String joinStringWithComplete(CharSequence delimiter,
                                    CharSequence prefix,
                                    CharSequence suffix, Object... array){
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (Object obj : array) {
            if (obj == null){
                joiner.add("null");
            }else if (obj instanceof CharSequence){
                joiner.add((CharSequence) obj);
            }else {
                Class<?> objClass = obj.getClass();
                if (char.class.equals(objClass)){
                    char[] chars = new char[]{(char) obj};
                    joiner.add(new String(chars));
                }else if (byte.class.equals(objClass)){
                    byte[] bytes = new byte[]{(byte) obj};
                    joiner.add(new String(bytes));
                }else if(byte[].class.equals(objClass)){
                    joiner.add(new String((byte[]) obj));
                }else if (char[].class.equals(objClass)){
                    joiner.add(new String((char[]) obj));
                }else {
                    joiner.add(obj.toString());
                }
            }
        }
        return joiner.toString();
    }

    public static String letString(Object... array){
        return letStringOfDef(null, array);
    }

    public static String letStringOfDef(String defaultValue, Object... array){
        StringBuilder builder = new StringBuilder();
        for (Object obj : array) {
            String str = getString(obj, defaultValue);
            if (str == null){
                str = "null";
            }
            builder.append(str);
        }
        return builder.toString();
    }


    public static String linkStr(String... strs) {
        StringBuilder builder = new StringBuilder();
        for (String str : strs) {
            if (str != null){
                builder.append(str);
            }
        }
        return builder.toString();
    }

    /** 首字母大写 */
    public static String titleCase(String str) {

        if (str == null || str.equals(""))
            return "";
        return str.substring(0,1).toUpperCase().concat(str.substring(1));
    }

    public static boolean hasText(String text){
        return text != null && !text.equals("");
    }

    public static String appendIfNotEmpty(String src, String txt){
        if (hasText(txt)){
            return src + txt;
        }
        return src;
    }

    public static String titleLower(String str){
        if (str == null || str.equals(""))
            return "";
        return str.substring(0,1).toLowerCase().concat(str.substring(1));
    }

    public static boolean matchesCharacter(@Nullable String str, char singleCharacter) {
        return (str != null && str.length() == 1 && str.charAt(0) == singleCharacter);
    }

    public static boolean hasLength(@Nullable String str) {
        return (str != null && !str.isEmpty());
    }

    public static String replace(String inString, String oldPattern, @Nullable String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }

        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0;  // our position in the old string
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        // append any characters to the right of a match
        sb.append(inString, pos, inString.length());
        return sb.toString();
    }

    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

}
