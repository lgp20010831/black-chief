package com.black.table;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.util.Assert;

import javax.sql.DataSource;

public abstract class AbstractDataSourceBuilder implements DataSourceBuilder {

    private DataSource dataSource;

    @Override
    public DataSource getDataSource() {
        if (dataSource == null){
            dataSource = createDataSource();
            Assert.notNull(dataSource, "datasource can not be null");
        }
        return dataSource;
    }

    protected abstract DataSource createDataSource();
}
