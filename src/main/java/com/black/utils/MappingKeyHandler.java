package com.black.utils;

public interface MappingKeyHandler {

    String uniqueIdentification(Class<?> methodParameterType, Class<?> parameterType);

    String[] uniqueIdentificationSupers(Class<?> methodParameterType, Class<?> parameterType);
}
