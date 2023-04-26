package com.black.core.sql.code.impl.config_impl;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.inter.ConfigurationAnnotationResolver;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.util.StringUtils;

public class ApplySqlResolver implements ConfigurationAnnotationResolver {
    @Override
    public void doReolver(Configuration configuration, ExecutePacket ep) {
        String applySql = configuration.getApplySql();
        String syntax = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getApplySql);
        if (StringUtils.hasText(applySql)){
            applySql = applySql + syntax;
        }else {
            applySql = syntax;
        }
        if (StringUtils.hasText(applySql)){
            applySql = GlobalMapping.parseAndObtain(applySql, true);
            applySql = MapArgHandler.parseSql(applySql, ep.getOriginalArgs());
            ep.getNhStatement().getStatement().writeLastSql(applySql);
        }


    }
}
