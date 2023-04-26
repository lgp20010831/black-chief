package com.black.core.sql.code;

public class InheritGlobalConvertHandler implements AliasColumnConvertHandler{
    @Override
    public String convertColumn(String alias) {
        throw new UnsupportedOperationException("please use a global converter instead");
    }

    @Override
    public String convertAlias(String columnName) {
        throw new UnsupportedOperationException("please use a global converter instead");
    }
}
