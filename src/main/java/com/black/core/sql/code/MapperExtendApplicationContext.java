package com.black.core.sql.code;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.sql.code.mapping.Mapper;
import com.black.core.sql.code.mapping.MapperImpl;
import lombok.NonNull;

import java.util.function.Supplier;

public class MapperExtendApplicationContext extends TransactionSQLApplicationContext{

    protected GlobalParentMapping proxyParentMapping;

    private MapperContextProxyManager contextProxyManager;

    public MapperExtendApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        super(configuration);
        contextProxyManager = new MapperContextProxyManager();
    }

    public MapperImpl getAndCreateImpl(Class<? extends Mapper> type){
        return contextProxyManager.getAndCreate(type, getConfiguration(), createParent(type));
    }

    protected GlobalParentMapping createParent(Class<?> type){
        if (proxyParentMapping == null){
            ClassWrapper<GlobalParentProxy> wrapper = ClassWrapper.get(GlobalParentProxy.class);
            ClassWrapper<?> classWrapper = ClassWrapper.get(type);
            proxyParentMapping = (GlobalParentMapping) agents.computeIfAbsent(GlobalParentProxy.class, c -> {
                MapperSQLProxy resolver;
                Object jdk = proxyFactory.proxy(c, resolver = new MapperSQLProxy(wrapper, getConfiguration()));
                agentReslovers.put(c, resolver);
                return jdk;
            });
            for (MethodWrapper mw : wrapper.getMethods()) {
                Supplier<Configuration> supplier = MapperRegister.getConfigurationSupplier(mw, classWrapper, getConfiguration());
                if (supplier != null){
                    bind(mw, wrapper, supplier);
                }
            }
        }
        return proxyParentMapping;
    }

    protected interface GlobalParentProxy extends GlobalParentMapping {

    }
}
