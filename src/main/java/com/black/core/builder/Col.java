package com.black.core.builder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.util.StringUtils;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("all")
public class Col {

    public static final String START_SYMBOL = "${";

    public static final String END_SYMBOL = "}";

    @SafeVarargs
    public static <A> A[] ay(int size, A... a){

        if (a.length > size)
            throw new IndexOutOfBoundsException("a.length should less than size");
        Object[] array = new Object[size];
        System.arraycopy(a, 0, array, 0, a.length);
        return (A[]) array;
    }
    public static <A> List<A> m(A[]... as){
        ArrayList<A> list = new ArrayList<>();
        for (A[] a : as) {
            list.addAll(Arrays.asList(a));
        }
        return list;
    }

    @SafeVarargs
    public static <T> List<T> as(T... element){
        return Arrays.asList(element);
    }
    public static JSONArray ja(Object... args){
        return new JSONArray(Arrays.asList(args));
    }
    public static <T> T[] ar(T... element){
        return element;
    }
    @SafeVarargs
    public static <S> Set<S> set(S... element){
        return new HashSet<>(as(element));
    }
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
    /** 将新 key 替换掉旧 key */
    public static <K, V> Map<K, V> rpk(@NonNull Map<K, V> source, @NonNull Map<K, K> condition){
        condition.forEach((nk, ok) ->{
            source.put(nk, source.get(ok));
            source.remove(ok);
        });
        return source;
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

    public static <T> boolean or(T t, T[] ts){
        for (T t1 : ts) {
            if (t1 != null){
                if (t1.equals(t)){
                    return true;
                }
            }
        }
        return false;
    }

    public static String in(String text, Object... params){
        StringBuilder builder = new StringBuilder();
        String[] sts = text.split("\\{}");
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
        builder.append("\n");
        return builder.toString();
    }

    public static String par(String entry, Map<String, String> source){
        if (entry.startsWith(START_SYMBOL)){

            if (!entry.endsWith(END_SYMBOL)){
                throw new IllegalStateException("entry " + entry + " missing terminator: }");
            }
            String attributeName = entry.substring(2, entry.length() - 1);
            return source.get(attributeName);
        }else {
            return entry;
        }
    }



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
    public static JSONObject jf(String[] ks, Object[] vs){
        JSONObject json = new JSONObject();
        int size = ks.length;
        if (size != vs.length){
            throw new RuntimeException("ks.size != vs.size");
        }
        for (int i = 0; i < ks.length; i++) {
            json.put(ks[i], vs[i]);
        }
        return json;
    }
    public interface ReplaceTypeKey<O, N>{
        N rtk(O o);
    }
    public static <N, O, V> Map<N, V> rt(Map<O, V> oldSource, ReplaceTypeKey<O, N> replaceTypeKey){
        Map<N, V> newMap = new HashMap<>();
        oldSource.forEach((o, v) -> newMap.put(replaceTypeKey.rtk(o), v));
        return newMap;
    }
    public static <K, V> Map<K, V> merge(@NonNull Map<K, V> s1, @NonNull Map<K, V> s2){
        Map<K, V> map = new HashMap<>(s1);
        map.putAll(s2);
        return map;
    }
    public static <K, V> Map<K, V> cv(@NonNull Map<K, V> source, @NonNull Map<K, K> condition){
        condition.forEach((k, nk) ->{
            if (source.containsKey(k)) {
                source.put(nk, source.get(k));
            }
        });
        return source;
    }

    public static <K, V> Map<K, V> as(K[] ks, V[] vs){
        Map<K, V> map = new HashMap<>();
        int size = ks.length;
        if (size != vs.length){
            throw new RuntimeException("ks.size != vs.size");
        }
        for (int i = 0; i < ks.length; i++) {
            map.put(ks[i], vs[i]);
        }
        return map;
    }

    public static Object gf(Object p, String k){
        Class<?> pClass = p.getClass();
        try {
            Field field = pClass.getDeclaredField(k);
            return field.get(p);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("获取字段: " + k + "失败", e);
        }
    }


}
