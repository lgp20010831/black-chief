package com.black.datasource;

import javax.sql.DataSource;

public interface ProduceElementResolver {

    DataSource tellApart(Object element) throws Throwable;

}
