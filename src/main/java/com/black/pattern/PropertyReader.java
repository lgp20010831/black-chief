package com.black.pattern;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.function.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-07-12 15:41
 */
@SuppressWarnings("all")
public class PropertyReader {

    public static void visitProperties(Object target, Consumer<Property> propertyConsumer){
        if (propertyConsumer == null) return;
        List<Property> properties = readProperties(target);
        for (Property property : properties) {
            try {
                propertyConsumer.accept(property);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static List<Property> readProperties(Object target){
        if (target == null){
            return new ArrayList<>();
        }
        if (target instanceof Class){
            return readClassProperties((Class) target, null);
        }

        if (target instanceof Map){
            return readMapProperties((Map<String, Object>) target);
        }

        return readClassProperties(BeanUtil.getPrimordialClass(target), target);
    }

    protected static List<Property> readClassProperties(Class clazz, Object host){
        ClassWrapper classWrapper = ClassWrapper.get(clazz);
        return StreamUtils.mapList(classWrapper.getFields(), fd -> {
            FieldWrapper f = (FieldWrapper) fd;
            Property property = new Property(f.getName(), clazz);
            property.setType(f.getType());
            property.setHost(host);
            return property;
        });
    }


    protected static List<Property> readMapProperties(Map<String, Object> map){
        List<Property> properties = new ArrayList<>();
        for (String key : map.keySet()) {
            Property property = new Property(key, map.getClass());
            property.setHost(map);
            properties.add(property);
        }
        return properties;
    }
}
