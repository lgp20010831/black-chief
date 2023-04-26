package com.black.core.sql.code.impl.appearance_impl;


import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.BoundDeletes;
import com.black.core.sql.code.config.BoundConfig;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.util.AnnotationUtils;

import java.sql.Connection;


public class DelectBoundAppearanceReolver extends AbstractBoundAppearanceResolver {

    @Override
    public boolean headSupport(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.DELETE &&
                configuration.getMethodWrapper().hasAnnotation(BoundDeletes.class);
    }

    @Override
    protected BoundConfig loadConfig(MethodWrapper mw, Configuration configuration, Connection connection) {
        BoundDeletes annotation = mw.getAnnotation(BoundDeletes.class);
        return AnnotationUtils.loadAttribute(annotation, new BoundConfig());
    }
}
