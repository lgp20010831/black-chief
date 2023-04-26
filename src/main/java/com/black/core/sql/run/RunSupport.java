package com.black.core.sql.run;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;

public interface RunSupport {

    boolean support(MethodWrapper mw);

    Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable;
}
