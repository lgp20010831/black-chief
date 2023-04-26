package com.black.core.sql.run;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.RefreshTable;
import com.black.core.sql.code.MapperSQLProxy;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.table.TableUtils;

import java.util.Map;

public class RefreshRunner implements RunSupport{
    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(RefreshTable.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        Map<Class<?>, MapperSQLProxy> agentReslovers = configuration.getApplicationContext().getAgentReslovers();
        TableUtils.clearCache();
        return null;
    }
}
