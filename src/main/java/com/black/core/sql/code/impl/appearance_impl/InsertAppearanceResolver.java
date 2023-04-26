package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.Insert;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.AppearanceFactory;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.List;

@Log4j2
public class InsertAppearanceResolver extends AbstractAppearanceResolver {

    @Override
    public boolean tailSupport(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.INSERT &&
                configuration.getMethodWrapper().hasAnnotation(Insert.class);
    }

    @Override
    protected List<AppearanceConfiguration> parse(MethodWrapper mw, Configuration configuration, Connection connection) {
        Insert insert = mw.getAnnotation(Insert.class);
        return AppearanceFactory.parse(insert.value(), configuration, connection);
    }
}
