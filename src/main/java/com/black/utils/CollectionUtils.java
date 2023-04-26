package com.black.utils;


import com.black.core.util.StreamUtils;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static <K, V> boolean isEmpty(Map<K, V> map){
        return map == null || map.isEmpty();
    }


    public static<K, V> K firstkey(Map<K, V> map){
        if (isEmpty(map)){
            return null;
        }
        return firstElement(map.keySet());
    }

    public static<K, V> V firstValue(Map<K, V> map){
        if (isEmpty(map)){
            return null;
        }
        return firstElement(map.values());
    }

    public static <K, V> Map<K, V> firstElement(Map<K, V> map){
        if (isEmpty(map)){
            return null;
        }

        K firstedElement = firstElement(map.keySet());
        return ServiceUtils.ofMap(firstedElement, map.get(firstedElement));
    }

    public static <T> T firstElement(Collection<T> collection){
        Iterator<T> iterator = collection.iterator();
        T ele = null;
        while (iterator.hasNext()) {
            ele = iterator.next();
            break;
        }
        return ele;
    }

    public static <T> T max(@NonNull Collection<T> collection, Function<T, Object> function){
        return max(collection, function, true);
    }

    public static <T> T max(@NonNull Collection<T> collection, Function<T, Object> function, boolean filterNull){
        List<T> list = new ArrayList<>(collection);
        if (filterNull){
            list = StreamUtils.filterList(list, u -> function.apply(u) != null);
        }
        ServiceUtils.sortInt(list, function, false, 0);
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> T min(@NonNull Collection<T> collection, Function<T, Object> function){
        return min(collection, function, true);
    }
    public static <T> T min(@NonNull Collection<T> collection, Function<T, Object> function, boolean filterNull){
        List<T> list = new ArrayList<>(collection);
        if (filterNull){
            list = StreamUtils.filterList(list, u -> function.apply(u) != null);
        }
        ServiceUtils.sortInt(list, function, true, 0);
        return list.isEmpty() ? null : list.get(0);
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList("1", "8", "10", "15", "3", "22", "6");
        System.out.println(max(list, s -> s));
    }
}
