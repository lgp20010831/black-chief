package com.black.utils;

import com.black.core.query.ClassWrapper;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class DefaultMappingKeyHandler implements MappingKeyHandler{

    private final Map<Class<?>, Map<Class<?>, String[]>> cache = new ConcurrentHashMap<>();

    @Override
    public String uniqueIdentification(Class<?> methodParameterType, Class<?> parameterType) {

        if (methodParameterType == null)
            throw new IllegalStateException("Mapping object for exception");

        if (parameterType == null)
            throw new IllegalStateException("Parameter type does not be," +
                    " it should convert");

        return StringUtils.linkStr(parameterType.getName(), "-- convert to -->", methodParameterType.getName());
    }
    @Override
    public String[] uniqueIdentificationSupers(Class<?> methodParameterType, Class<?> parameterType) {
        if (methodParameterType == null)
            throw new IllegalStateException("Mapping object for exception");

        if (parameterType == null)
            throw new IllegalStateException("Parameter type does not be," +
                    " it should convert");

        Map<Class<?>, String[]> classMap = cache.computeIfAbsent(methodParameterType, mpt -> new ConcurrentHashMap<>());
        if (classMap.containsKey(parameterType)){
            return classMap.get(parameterType);
        }

        List<Class<?>> supers = new ArrayList<>();
        Class<?> superClazz =  parameterType;
        for (;;){
            if(superClazz == null){
                break;
            }
            if(ClassWrapper.isBasic(superClazz.getName())){
                supers.add(ClassWrapper.pack(superClazz.getName()));
            }

            if (ClassWrapper.isBasicWrapper(superClazz.getName())){
                supers.add(ClassWrapper.unpacking(superClazz.getSimpleName()));
            }
            supers.add(superClazz);
            supers.addAll(Arrays.asList(superClazz.getInterfaces()));
            superClazz = superClazz.getSuperclass();
        }
        String[] entry = new String[supers.size()];
        for (int i = 0; i < supers.size(); i++) {
            entry[i] = uniqueIdentification(methodParameterType, supers.get(i));
        }
        classMap.put(parameterType, entry);
        return entry;
    }
}
