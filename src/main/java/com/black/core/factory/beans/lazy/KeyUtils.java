package com.black.core.factory.beans.lazy;

import com.black.core.factory.beans.annotation.Key;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Utils;
import com.black.utils.NameUtil;
import com.black.utils.ServiceUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 李桂鹏
 * @create 2023-05-23 17:44
 */
@SuppressWarnings("all")
public class KeyUtils {

    public interface EnvironmentalPreparer<S>{

        void prepare(S s, Map<String, Object> env);
    }

    public static <V> Map<String, V> handlerKey(AnnotatedElement element,
                                                Collection<V> sources){
        return handlerKey(element, sources, null);
    }
    
    public static <V> Map<String, V> handlerKey(AnnotatedElement element,
                                                Collection<V> sources,
                                                EnvironmentalPreparer<V> preparer){
        if (Utils.isEmpty(sources)){
            return new LinkedHashMap<>();
        }

        LinkedHashMap<String, V> resultMap = new LinkedHashMap<>();
        Key annotation = element.getAnnotation(Key.class);
        if (annotation == null){
            for (V source : sources) {
                resultMap.put(NameUtil.getName(source), source);
            }
        }else {
            String expression = annotation.value();
            for (V source : sources) {
                Map<String, Object> env = new LinkedHashMap<>();
                env.put("source", source);
                env.put("sourceType", BeanUtil.getPrimordialClass(source));
                if (preparer != null){
                    preparer.prepare(source, env);
                }
                String mapKey = ServiceUtils.patternGetValue(env, expression);
                resultMap.put(mapKey, source);
            }
        }
        return resultMap;
    }
    
}
