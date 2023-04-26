package com.black.core.sql.run;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ObtainConnection;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.table.jdbc.CatalogConnection;
import com.black.table.jdbc.CatalogDataSource;
import com.black.table.jdbc.CatalogDataSourceFactory;
import com.black.table.jdbc.TemporaryCatalogConnection;

import javax.sql.DataSource;
import java.sql.Connection;

public class ObtainConnectionRunner implements RunSupport{
    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(ObtainConnection.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        Class<?> returnType = mw.getReturnType();
        if (Connection.class.isAssignableFrom(returnType)){
            return configuration.getConnection();
        }else if (CatalogConnection.class.equals(returnType)){
            DataSource dataSource = configuration.getDataSource();
            CatalogDataSource catalogDataSource = CatalogDataSourceFactory.createDataSource(dataSource);
            return new TemporaryCatalogConnection(catalogDataSource, configuration.getConnection());
        }
        return null;
    }
}
