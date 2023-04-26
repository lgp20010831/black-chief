package com.black.core.builder;

import com.black.core.json.NotNull;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Log4j2
public class SortUtil {

    public static <V, T extends List<V>> T sort(T source){
        return sort(null, source, true);
    }
    public static <V, T extends List<V>> T sort(T source, boolean asce){
        return sort(null, source, asce);
    }

    public static <V, T extends List<V>> T sort(String sortBasisName, T source){
        return sort(sortBasisName, source, true);
    }

    @SuppressWarnings("all")
    public static <V, T extends List<V>> T sort(String sortBasisName, T source, boolean asce){

        if (source == null)
            return null;

        if (source.isEmpty())
            return source;

        source.sort(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {

                if (o1 == null || o2 == null)
                    return 0;

                if (o1 instanceof String){
                    return asce ? Integer.parseInt(o1.toString())  - Integer.parseInt(o2.toString())
                            : Integer.parseInt(o2.toString()) - Integer.parseInt(o1.toString());
                }else if (o1 instanceof Integer)
                    return asce ? (Integer) o1 - (Integer) o2 : (Integer) o2 - (Integer) o1;

                else {

                    try {

                        if (sortBasisName == null)
                            return 0;

                        return compare(findSort(sortBasisName, o1), findSort(sortBasisName, o2));
                    } catch (RuntimeException e) {

                        if (log.isWarnEnabled()) {
                            log.warn("无效的排序, 匹配不到特殊类型且找不到指定的字段或是无法获取字段里的信息");
                        }
                        return 0;
                    }
                }
            }
        });

        return source;
    }


    public static Object findSort(String sort, Object source){

        String[] sorts = sort.split("\\.");

        Object tempSource = source;
        for (String s : sorts) {

            if (tempSource instanceof Map){

                if (!((Map<?, ?>) tempSource).containsKey(s))
                    return null;
                tempSource =  ((Map<?, ?>) tempSource).get(s);
                continue;
            }

            tempSource = getFieldValue(s, tempSource);
        }
        return tempSource;
    }

    public static Object getFieldValue(@NotNull String fieldName, @NotNull Object obj){
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
