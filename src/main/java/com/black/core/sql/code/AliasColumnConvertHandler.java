package com.black.core.sql.code;

public interface AliasColumnConvertHandler {

    String convertColumn(String alias);

    String convertAlias(String columnName);
}
