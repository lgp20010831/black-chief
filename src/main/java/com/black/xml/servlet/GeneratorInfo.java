package com.black.xml.servlet;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-06-02 10:56
 */
@SuppressWarnings("all") @AllArgsConstructor @Data
public class GeneratorInfo {

    private final Object instance;

    private final Class<?> clazz;

    private final String classInfo;
}
