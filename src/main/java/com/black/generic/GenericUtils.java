package com.black.generic;

import com.alibaba.fastjson.JSONObject;
import com.black.utils.CollectionUtils;
import lombok.NonNull;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-09 11:08
 */
@SuppressWarnings("all")
public class GenericUtils {


    public static GenericInfo getGenericByField(@NonNull Field field){
        Type genericType = field.getGenericType();
        GenericInfo genericInfo = new GenericInfo();
        if (genericType instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            List<Generic> generics = parseTypes(actualTypeArguments);
            generics.forEach(genericInfo::addGeneric);
        }
        return genericInfo;
    }

    public static GenericInfo getGenericByParameter(Parameter parameter){
        Type genericType = parameter.getParameterizedType();
        GenericInfo genericInfo = new GenericInfo();
        if (genericType instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            List<Generic> generics = parseTypes(actualTypeArguments);
            generics.forEach(genericInfo::addGeneric);
        }
        return genericInfo;
    }

    public static GenericInfo getGenericByMethodReturnType(Method method){
        Type genericType = method.getGenericReturnType();
        GenericInfo genericInfo = new GenericInfo();
        if (genericType instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            List<Generic> generics = parseTypes(actualTypeArguments);
            generics.forEach(genericInfo::addGeneric);
        }
        return genericInfo;
    }

    public static GenericInfo getGenericByInterface(Class<?> targetClass, Class<?> achieveInterface){
        Type[] genericTypes = targetClass.getGenericInterfaces();
        GenericInfo genericInfo = new GenericInfo();
        for (Type genericType : genericTypes) {
            if (!loadType(genericType).equals(achieveInterface)) {
                continue;
            }
            if (genericType instanceof ParameterizedType){
                Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                List<Generic> generics = parseTypes(actualTypeArguments);
                generics.forEach(genericInfo::addGeneric);
            }
        }

        return genericInfo;
    }

    public static GenericInfo getGenericBySuper(Class<?> targetClass){
        Type genericType = targetClass.getGenericSuperclass();
        GenericInfo genericInfo = new GenericInfo();
        if (genericType instanceof ParameterizedType){
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            List<Generic> generics = parseTypes(actualTypeArguments);
            generics.forEach(genericInfo::addGeneric);
        }
        return genericInfo;
    }

    public static List<Generic> parseTypes(Type[] types){
        List<Generic> genericList = new ArrayList<>();
        for (Type type : types) {
            Generic generic = new Generic(loadType(type));
            if (type instanceof ParameterizedType){
                List<Generic> generics = parseTypes(((ParameterizedType) type).getActualTypeArguments());
                generics.forEach(generic::addGeneric);
            }
            genericList.add(generic);
        }
        return genericList;
    }

    public static Class<?> loadType(@NonNull Type type){
        if (type instanceof Class){
            return (Class<?>) type;
        }

        if (type instanceof TypeVariableImpl){
            Type[] bounds = ((TypeVariableImpl<?>) type).getBounds();
            if (bounds.length == 1){
                Type bound = bounds[0];

                if (bound instanceof Class){
                    return (Class<?>) bound;
                }

                if (bound instanceof ClassTypeSignature){
                    List<SimpleClassTypeSignature> signatureList = ((ClassTypeSignature) bound).getPath();
                    SimpleClassTypeSignature typeSignature = CollectionUtils.firstElement(signatureList);
                    if (typeSignature != null){
                        return loadTypeByName(typeSignature.getName());
                    }
                }
            }
        }
        return loadTypeByName(type.getTypeName());
    }

    protected static Class<?> loadTypeByName(@NonNull String name){
        try {
            int s = name.indexOf("<");
            if (s != -1){
                name = name.substring(0, s);
            }
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("load class fail", e);
        }
    }

}
