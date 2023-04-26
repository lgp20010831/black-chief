package com.black.core.sql;

import com.black.core.sql.code.AliasColumnConvertHandler;

public class DefaultNoParseAliasAndColumnHandler implements AliasColumnConvertHandler {

    @Override
    public String convertColumn(String alias) {
        return alias;
    }

    @Override
    public String convertAlias(String columnName) {
        return columnName;
    }
}
