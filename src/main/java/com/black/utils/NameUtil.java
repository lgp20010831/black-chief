package com.black.utils;

import com.black.core.json.Alias;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

public class NameUtil {

    public static String getName(Object obj){
        if (obj == null){
            return null;
        }
        Class<?> targetClazz;
        if (obj instanceof Class){
            targetClazz = (Class<?>) obj;
        }else {
            targetClazz = BeanUtil.getPrimordialClass(obj);
        }
        Alias alias = AnnotationUtils.getAnnotation(targetClazz, Alias.class);
        return alias == null ? StringUtils.titleLower(targetClazz.getSimpleName()) : alias.value();
    }

}
