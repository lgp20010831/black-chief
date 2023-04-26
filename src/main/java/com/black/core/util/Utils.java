package com.black.core.util;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.sql.code.mapping.MappingException;

import java.sql.Types;
import java.util.*;

public class Utils {

    public static void sleep(long t){
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }

    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");

    }

    public static String parseTopic(String topic){
        if (topic.startsWith("${")){

            if (!topic.endsWith("}")){
                throw new IllegalStateException("主题 " + topic + " 缺少结束符: }");
            }
            String attributeName = topic.substring(2, topic.length() - 1);
            ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
            return reader.selectAttribute(attributeName);
        }else {
            return topic;
        }
    }

    public static String parse(String txt, String start, String end, Map<String, String> argMap){
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
            String v = argMap.get(key);
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
        return afterParse;
    }

    public static <K, V> Map<String, String> castMap(Map<K, V> map){
        return (Map<String, String>) map;
    }

    public static <B, E extends Collection<B>> E addAll(E e, B[] array){
        e.addAll(Arrays.asList(array));
        return e;
    }

    public static Class<?> convertType(int jdbcType){
        switch (jdbcType){
            case Types.INTEGER:
                return Integer.class;
            case Types.DOUBLE:
            case Types.FLOAT:
                return Double.class;
            case Types.BLOB:
            case Types.BOOLEAN:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    public static Character[] toCharacterArray( String s ) {

        if ( s == null ) {
            return null;
        }

        int len = s.length();
        Character[] array = new Character[len];
        for (int i = 0; i < len ; i++) {
      /*
      Character(char) is deprecated since Java SE 9 & JDK 9
      Link: https://docs.oracle.com/javase/9/docs/api/java/lang/Character.html
      array[i] = new Character(s.charAt(i));
      */
            array[i] = s.charAt(i);
        }

        return array;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static <K, V> boolean isEmpty(Map<K, V> map){
        return map == null || map.isEmpty();
    }

    public static <T> List<T> wrapperList(T t){
        List<T> list = new ArrayList<>();
        if (t != null){
            list.add(t);
        }
        return list;
    }

    public static <T> T firstElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).first();
        }

        Iterator<T> it = set.iterator();
        T first = null;
        if (it.hasNext()) {
            first = it.next();
        }
        return first;
    }


}
