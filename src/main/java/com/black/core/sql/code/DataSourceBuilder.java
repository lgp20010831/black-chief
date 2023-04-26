package com.black.core.sql.code;

import javax.sql.DataSource;

public interface DataSourceBuilder {

    DataSource getDataSource();


    default void close(){

    }
}
