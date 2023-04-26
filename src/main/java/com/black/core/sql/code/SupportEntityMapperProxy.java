package com.black.core.sql.code;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentObject;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.entity.EntityImpl;
import com.black.core.sql.entity.EntityMapper;

import java.lang.reflect.Method;

public class SupportEntityMapperProxy extends MapperSQLProxy{

    public SupportEntityMapperProxy(ClassWrapper<?> wrapper, GlobalSQLConfiguration configuration) {
        super(wrapper, configuration);
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        Method method = layer.getProxyMethod();
        if (isEntityMethod(method)){
            return invokeEntityMethod(layer);
        }
        return super.proxy(layer);
    }

    public boolean isEntityMethod(Method method){
        return EntityMapper.class.equals(method.getDeclaringClass());
    }

    private Object invokeEntityMethod(AgentObject layer){
        Class<?> agentClazz = layer.getAgentClazz();
        SQLApplicationContext context = getConfiguration().getApplicationContext();
        EntityImpl impl;
        if (context instanceof EntityNapperApplicationContext){
            impl = ((EntityNapperApplicationContext) context).getAndCreate((Class<? extends EntityMapper<?>>) agentClazz);
        }else if (context instanceof EntityXmlNapperApplicationContext){
            impl = ((EntityXmlNapperApplicationContext) context).getAndCreate((Class<? extends EntityMapper<?>>) agentClazz);
        }else {
            throw new IllegalStateException("unknown entity context: " + context);
        }
        MethodWrapper mw = MethodWrapper.get(layer.getProxyMethod());
        return mw.invoke(impl, layer.getArgs());
    }
}
