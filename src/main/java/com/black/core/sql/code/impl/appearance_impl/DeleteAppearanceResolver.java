package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.Delete;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.AppearanceFactory;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.List;

@Log4j2
public class DeleteAppearanceResolver extends AbstractAppearanceResolver {

    @Override
    public boolean headSupport(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.DELETE &&
                configuration.getMethodWrapper().hasAnnotation(Delete.class);
    }

    @Override
    protected List<AppearanceConfiguration> parse(MethodWrapper mw, Configuration configuration, Connection connection) {
        Delete delete = mw.getAnnotation(Delete.class);
        return AppearanceFactory.parse(delete.value(), configuration, connection);
    }
}
