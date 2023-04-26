package com.black.core.sql.run;


import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.DoNothing;
import com.black.core.sql.code.config.GlobalSQLConfiguration;

public class DoNothingProcessor implements RunSupport{


    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(DoNothing.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        return null;
    }
}
