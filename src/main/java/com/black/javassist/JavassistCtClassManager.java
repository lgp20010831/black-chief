package com.black.javassist;

import com.black.function.Supplier;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavassistCtClassManager {

    //key = 唯一的描述信息, value = 动态生产的 class
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    public static void registerJavassistClass(String desc, Class<?> type){
        if (desc != null && type != null){
            classCache.put(desc, type);
        }
    }

    public static Class<?> tryGetCtClass(@NonNull String desc){
        return classCache.get(desc);
    }

    public static Class<?> getCtClass(@NonNull String desc, @NonNull Supplier<Class<?>> createFunction){
        return classCache.computeIfAbsent(desc, d -> {
            try {
                return createFunction.get();
            } catch (Throwable e) {
                throw new IllegalStateException("CREATE CT CLASS FAIL FOR DESC: " + d, e);
            }
        });
    }


}
