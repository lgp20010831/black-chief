package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.inter.AppearanceResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.factory.PacketFactory;
import com.black.utils.LocalMap;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.List;

@Log4j2
public abstract class AbstractAppearanceResolver implements AppearanceResolver {

    private final LocalMap<String, List<AppearanceConfiguration>> listLocalMap = new LocalMap<>();

    @Override
    public void doHeadAppearance(AbstractSqlsPipeNode node, Configuration configuration, ExecutePacket ep) {
        Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
        MethodWrapper methodWrapper = configuration.getMethodWrapper();
        String tableName = configuration.getTableName();
        List<AppearanceConfiguration> list = listLocalMap.computeIfAbsent(tableName, m -> {
            return parse(methodWrapper, configuration, connection);
        });
        if (list == null) return;
        for (AppearanceConfiguration appearanceConfiguration : list) {
            ExecutePacket packet = PacketFactory.createPacket(appearanceConfiguration);
            try {
                appearanceConfiguration.setEp(ep);

                //将原本 ep 的属性加载到新创建的 ep
                if (!interceptBeforePipeline(packet.transfer(ep), appearanceConfiguration)){
                    node.getPipeline().headfire(packet);
                }
            }catch (SQLSException e){
                if (e instanceof StopSqlInvokeException){
                    if (log.isInfoEnabled()) {
                        log.info("sql break, reason:[{}]", e.getMessage());
                    }
                }else
                    throw e;
            }
            postAfterPipeline(packet, appearanceConfiguration);
        }
    }

    @Override
    public void doTailAppearance(AbstractSqlsPipeNode node, Configuration configuration, ResultPacket rp) {
        Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
        MethodWrapper methodWrapper = configuration.getMethodWrapper();
        List<AppearanceConfiguration> list = listLocalMap.computeIfAbsent(configuration.getTableName(), m -> {
            return parse(methodWrapper, configuration, connection);
        });
        if (list == null) return;
        for (AppearanceConfiguration appearanceConfiguration : list) {
            ExecutePacket packet = PacketFactory.createPacket(appearanceConfiguration);
            try {
                appearanceConfiguration.setEp(rp.getEp());
                if (!interceptBeforePipeline(packet.transfer(rp.getEp()), appearanceConfiguration)){
                    node.getPipeline().headfire(packet);
                }

            }catch (SQLSException e){
                if (e instanceof StopSqlInvokeException){
                    if (log.isInfoEnabled()) {
                        log.info("sql break, reason:[{}]", e.getMessage());
                    }
                }else
                    throw e;
            }
            postAfterPipeline(packet, appearanceConfiguration);
        }
    }

    protected boolean interceptBeforePipeline(ExecutePacket ep, AppearanceConfiguration configuration){
        return false;
    }

    protected void postAfterPipeline(ExecutePacket ep, AppearanceConfiguration configuration){

    }

    protected abstract List<AppearanceConfiguration> parse(MethodWrapper mw, Configuration configuration, Connection connection);
}
