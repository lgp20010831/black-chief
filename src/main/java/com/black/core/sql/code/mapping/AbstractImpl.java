package com.black.core.sql.code.mapping;

import com.black.core.sql.code.config.GlobalSQLConfiguration;

public abstract class AbstractImpl {

    private final GlobalSQLConfiguration configuration;

    private final GlobalParentMapping parentMapping;

    private String tableName;

    private String primaryKeyName;

    private String primaryColumnName;

    protected AbstractImpl(GlobalSQLConfiguration configuration, GlobalParentMapping parentMapping) {
        this.configuration = configuration;
        this.parentMapping = parentMapping;
    }

}
