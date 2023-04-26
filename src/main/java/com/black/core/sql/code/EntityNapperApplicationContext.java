package com.black.core.sql.code;

import com.black.core.query.ClassWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.entity.EntityImpl;
import com.black.core.sql.entity.EntityMapper;
import lombok.NonNull;

public class EntityNapperApplicationContext extends AnnotationMapperSQLApplicationContext{


    private final EntityContextProxyManager contextProxyManager;

    public EntityNapperApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        super(configuration);
        contextProxyManager = new EntityContextProxyManager();
    }

    @Override
    protected MapperSQLProxy createMapperProxy(ClassWrapper<?> cw) {
        return new SupportEntityMapperProxy(cw, getConfiguration());
    }

    public EntityImpl getAndCreate(Class<? extends EntityMapper<?>> type){
        return contextProxyManager.getAndCreate(type, getConfiguration(), createParent(type));
    }

}
