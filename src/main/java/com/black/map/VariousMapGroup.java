package com.black.map;

import com.black.core.json.ReflexUtils;

import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.vfs.VfsLoader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VariousMapGroup<K> {

    private final Map<K, AbstractVariousMap<?>> variousMapManager = new ConcurrentHashMap<>();

    private VfsLoader loader;

    public void scan(String packageName){
        if (loader == null)
            loader = new VfsLoader();

        Set<Class<?>> classes = loader.load(packageName);
        for (Class<?> clazz : classes) {
            if (AbstractVariousMap.class.isAssignableFrom(clazz) && BeanUtil.isSolidClass(clazz)){
                if (clazz.isAnnotationPresent(VariousMark.class)) {
                    VariousMark mark = clazz.getAnnotation(VariousMark.class);
                    String text = mark.value();
                    if (StringUtils.hasText(text)){
                        register((K) text, createInstance((Class<? extends AbstractVariousMap<?>>) clazz));
                    }else {
                        Class<? extends ObtainVariousMark> markprovider = mark.markprovider();
                        if (!markprovider.equals(ObtainVariousMark.class)){
                            ObtainVariousMark obtainVariousMark = ReflexUtils.instance(markprovider);
                            register((K) obtainVariousMark.mark(), createInstance((Class<? extends AbstractVariousMap<?>>) clazz));
                        }else {
                            Class<? extends Annotation> type = mark.customAnnotationType();
                            if (type.equals(VariousMark.class)){
                                throw new IllegalStateException("无状态可读取");
                            }

                            Annotation annotation = clazz.getAnnotation(type);
                            if (annotation == null){
                                throw new IllegalStateException("指定注解不存在");
                            }

                            Object value = AnnotationUtils.getValueFromAnnotation(annotation);
                            register((K) value, createInstance((Class<? extends AbstractVariousMap<?>>) clazz));
                        }
                    }
                }
            }
        }
    }

    protected AbstractVariousMap<?> createInstance(Class<? extends AbstractVariousMap<?>> varClass){
        return ReflexUtils.instance(varClass);
    }

    public void register(K k, AbstractVariousMap<?> map){
        variousMapManager.put(k, map);
    }

    public AbstractVariousMap<?> take(K k){
        return variousMapManager.get(k);
    }

    public void clear(){
        variousMapManager.clear();
    }
}
