package com.black.core.sql.code.impl.sqlarg_impl;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;

public abstract class AbstractCommonArguramentResolver extends AbstractArguramentResolver {

    @Override
    public void doResolver(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw) {
        if (!(configuration instanceof AppearanceConfiguration)){
            super.doResolver(configuration, ep, value, pw);
        }
    }



}
