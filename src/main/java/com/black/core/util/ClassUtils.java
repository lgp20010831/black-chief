package com.black.core.util;


import com.black.core.query.ClassWrapper;
import lombok.NonNull;


public class ClassUtils {

    private static final char PACKAGE_SEPARATOR = '.';

    public static ClassWrapper<?> getClassWrapper(@NonNull Class<?> clazz){
        return ClassWrapper.get(clazz);
    }

    public static String getPackageName(Class<?> clazz) {

        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }
}
