package com.black.jdbc;

public class NativeSqlConstructor implements SqlConstructor{

    private final String sql;

    public NativeSqlConstructor(String sql) {
        this.sql = sql;
    }

    @Override
    public String createSql() {
        return sql;
    }
}
