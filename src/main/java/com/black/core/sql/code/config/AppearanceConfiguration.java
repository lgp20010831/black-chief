package com.black.core.sql.code.config;


import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.table.ForeignKey;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter @Setter
public class AppearanceConfiguration extends Configuration{

    //要操作的从表
    final TableMetadata processorMetadata;

    final TableMetadata masterMetadata;

    final PrimaryKey primaryKey;

    final Configuration configuration;

    private String appearanceName;

    private ExecutePacket ep;

    private ForeignKey key;

    private boolean relyAppearance;

    public AppearanceConfiguration(TableMetadata processorMetadata, String foreignKeyName, Configuration configuration) {
        super(configuration.getGlobalSQLConfiguration(), configuration.getMethodWrapper());
        this.processorMetadata = processorMetadata;
        this.configuration = configuration;
        key = processorMetadata.getForeignKey(foreignKeyName);
        if (key == null){
            throw new SQLSException("外键不存在: " + foreignKeyName);
        }
        primaryKey = key.getMappingPrimaryKey();
        masterMetadata = primaryKey.getRawTableMetadata();
    }


    public String getTargetKey(){
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        return handler.convertAlias(primaryKey.getName());
    }

    public String getForeignKeyColumnName(){
        return key.getName();
    }

    @Override
    public String getTableName() {
        return processorMetadata.getTableName();
    }

    @Override
    public boolean isFilterNullValueKey() {
        return configuration.isFilterNullValueKey();
    }

    @Override
    public GlobalSQLConfiguration getGlobalSQLConfiguration() {
        return configuration.getGlobalSQLConfiguration();
    }

    @Override
    public TableMetadata getTableMetadata() {
        return processorMetadata;
    }

    @Override
    public SQLSignalSession getSession() {
        return configuration.getSession();
    }

    @Override
    public Class<? extends Map> getMapType() {
        return configuration.getMapType();
    }

    @Override
    public SQLMethodType getMethodType() {
        return configuration.getMethodType();
    }

    @Override
    public Set<String> getColumnNames() {
        return processorMetadata.getColumnNameSet();
    }

    @Override
    public Set<String> getDataAliases() {
        return configuration.getDataAliases();
    }

    @Override
    public ClassWrapper<?> getCw() {
        return configuration.getCw();
    }

    @Override
    public String getApplySql() {
        return configuration.getApplySql();
    }

    @Override
    public AliasColumnConvertHandler getColumnConvertHandler() {
        return configuration.getColumnConvertHandler();
    }

    @Override
    public MethodWrapper getMethodWrapper() {
        return configuration.getMethodWrapper();
    }

}
