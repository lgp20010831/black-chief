package com.black.core.db;

import com.black.db.DbBufferManager;
import com.black.db.MapSqlDBConnection;
import com.black.db.SpringDBConnection;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.sql.code.AnnotationMapperSQLApplicationContext;
import com.black.core.sql.code.MapperRegister;

import java.util.Map;

@LoadSort(12458)
public class DbComponent implements OpenComponent {

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {
        if (MapperRegister.isTrip()) {
            MapperRegister register = MapperRegister.getRegister();
            Map<String, AnnotationMapperSQLApplicationContext> contextCache = register.getContextCache();
            for (String alias : contextCache.keySet()) {
                MapSqlDBConnection dbConnection = new MapSqlDBConnection(alias);
                DbBufferManager.alloc(dbConnection);
            }
        }
        SpringDBConnection dbConnection = new SpringDBConnection();
        DbBufferManager.alloc(dbConnection);
    }

}
