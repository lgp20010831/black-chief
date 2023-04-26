package com.black.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.black.core.builder.Col;
import lombok.NonNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class Vfu {

    @SafeVarargs
    public static <A> List<A> as(A... as){
        return new ArrayList<>(Arrays.asList(as));
    }

    public static JSONArray ja(Object... args){
        return new JSONArray(Arrays.asList(args));
    }

    @SafeVarargs
    public static Map<String, Object> merge(Map<String, Object>... sources){
        Map<String, Object> map = new HashMap<>();
        for (Map<String, Object> source : sources) {
            map.putAll(source);
        }
        return map;
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
        StringUtils.removeIfStartWith(str, "_");
        return str;
    }

    public static String ruacnl(String str){
        if (!str.contains("_")){
            return str;
        }
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean d = false;
        for (char c : chars) {
            if (c == '_'){
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

    @SafeVarargs
    public static Body merge0(Map<String, Object>... sources){
        Body map = new Body();
        for (Map<String, Object> source : sources) {
            map.putAll(source);
        }
        return map;
    }

    @SafeVarargs
    public static <T> T[] ar(T... element){
        return element;
    }
    public static JSONObject cj(Map<String, Object> source){
        return source == null ? new JSONObject() : new JSONObject(source);
    }

    public static Body bw(Map<String, Object> map){
        return map == null ? new Body() : new Body(map);
    }

    @SafeVarargs
    public static <S> Set<S> set(S... element){
        return new HashSet<>(as(element));
    }
    public static <K, V> Map<K, V> of(){return (Map<K, V>) as(new Object[0], new Object[0]);}
    public static <K, V> Map<K, V> of(K k1, V v1){
        return (Map<K, V>) as(new Object[]{k1}, new Object[]{v1});
    }
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2){
        return (Map<K, V>) as(new Object[]{k1, k2}, new Object[]{v1, v2});
    }
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3){
        return (Map<K, V>) as(new Object[]{k1, k2, k3}, new Object[]{v1, v2, v3});
    }
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4){
        return (Map<K, V>) as(new Object[]{k1, k2, k3, k4}, new Object[]{v1, v2, v3, v4});
    }
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5){
        return (Map<K, V>) as(new Object[]{k1, k2, k3, k4, k5}, new Object[]{v1, v2, v3, v4, v5});
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6){
        return (Map<K, V>) as(new Object[]{k1, k2, k3, k4, k5, k6}, new Object[]{v1, v2, v3, v4, v5, v6});
    }
    private static <K, V> Map<K, V> as(K[] ks, V[] vs){
        return Col.as(ks, vs);
    }

    public static JSONObject js(){return jf(new String[0], new String[0]);}
    public static JSONObject js(String k1, Object v1){
        return jf(new String[]{k1}, new Object[]{v1});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2){
        return jf(new String[]{k1, k2}, new Object[]{v1, v2});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3){
        return jf(new String[]{k1, k2, k3}, new Object[]{v1, v2, v3});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4){
        return jf(new String[]{k1, k2, k3, k4}, new Object[]{v1, v2, v3, v4});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5){
        return jf(new String[]{k1, k2, k3, k4, k5}, new Object[]{v1, v2, v3, v4, v5});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6){
        return jf(new String[]{k1, k2, k3, k4, k5, k6}, new Object[]{v1, v2, v3, v4, v5, v6});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7){
        return jf(new String[]{k1, k2, k3, k4, k5, k6, k7}, new Object[]{v1, v2, v3, v4, v5, v6, v7});
    }
    public static JSONObject js(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7, String k8, Object v8){
        return jf(new String[]{k1, k2, k3, k4, k5, k6, k7, k8}, new Object[]{v1, v2, v3, v4, v5, v6, v7, v8});
    }

    public static JSONObject jf(String[] ks, Object[] vs){
        return Col.jf(ks, vs);
    }


    /** 将新 key 替换掉旧 key */
    public static <K, V> Map<K, V> rpk(@NonNull Map<K, V> source, @NonNull Map<K, K> condition){
        return Col.rpk(source, condition);
    }

    public static Map<String, String> uk(Map<String, Object> s){
        Map<String, String> result = new HashMap<>();
        s.forEach((k, v) ->result.put(u(k), k));
        return result;
    }

    public static JSONArray gA(JSONObject body, String key){
        JSONArray jsonArray = body.getJSONArray(key);
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

    public static String u(String s){
        return StringUtils.removeUnder(s).toLowerCase();
    }
    /** = filter null  */
    public static <K, V>  Map<K, V> fn(Map<K, V> source){
        if (source == null){
            return null;
        }
        Iterator<K> ki = source.keySet().iterator();
        while (ki.hasNext()) {
            K k = ki.next();
            if (source.get(k) == null) {
                ki.remove();
                source.remove(k);
            }
        }
        return source;
    }


    public static String now(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
