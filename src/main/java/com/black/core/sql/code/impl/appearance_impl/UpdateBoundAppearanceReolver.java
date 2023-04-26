package com.black.core.sql.code.impl.appearance_impl;


import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.BoundUpdates;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.BoundConfig;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Utils;

import java.sql.Connection;
import java.util.Collection;


public class UpdateBoundAppearanceReolver extends AbstractBoundAppearanceResolver {

    @Override
    public boolean tailSupport(Configuration configuration) {
        return !(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.UPDATE &&
                configuration.getMethodWrapper().hasAnnotation(BoundUpdates.class);
    }

    @Override
    protected BoundConfig loadConfig(MethodWrapper mw, Configuration configuration, Connection connection) {
        BoundUpdates annotation = mw.getAnnotation(BoundUpdates.class);
        return AnnotationUtils.loadAttribute(annotation, new BoundConfig());
    }

    @Override
    protected boolean interceptBeforePipeline(ExecutePacket ep, AppearanceConfiguration configuration) {
        Object result = SQLUtils.loopFind(ep.getOriginalArgs(), configuration.getAppearanceName());
        if (result == null){
            return true;
        }
        if (result instanceof Collection<?>){
            return Utils.isEmpty((Collection<?>) result);
        }
        return false;
    }
}
