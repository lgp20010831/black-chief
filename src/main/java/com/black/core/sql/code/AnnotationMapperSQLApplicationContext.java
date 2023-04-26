package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import lombok.NonNull;


public class AnnotationMapperSQLApplicationContext extends MapperExtendApplicationContext{

    public AnnotationMapperSQLApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        super(configuration);
    }

}
