package com.black.jdbc;

public class JavaClassSqlConstructor implements SqlConstructor{

    private final Class<?> entity;

    public JavaClassSqlConstructor(Class<?> entity) {
        this.entity = entity;
    }

    @Override
    public String createSql() {
        return null;
    }
}
