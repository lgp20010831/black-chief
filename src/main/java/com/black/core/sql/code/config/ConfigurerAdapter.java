package com.black.core.sql.code.config;

import com.black.core.query.ClassWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.table.TableMetadata;
import lombok.NonNull;

import java.sql.Connection;
import java.util.Collection;
import java.util.Set;

public class ConfigurerAdapter extends Configuration{

    Configuration parent;

    public ConfigurerAdapter(@NonNull Configuration configuration) {
        super(configuration.getGlobalSQLConfiguration(), configuration.getMethodWrapper());
        parent = configuration;
    }


    @Override
    public SQLSignalSession getSession() {
        return parent.getSession();
    }

    @Override
    public ClassWrapper<?> getCw() {
        return parent.getCw();
    }

    @Override
    public String getTableName() {
        return parent.getTableName();
    }

    @Override
    public String convertColumn(String alias) {
        return parent.convertColumn(alias);
    }

    @Override
    public String convertAlias(String column) {
        return parent.convertAlias(column);
    }

    @Override
    public Log getLog() {
        return parent.getLog();
    }

    @Override
    public Collection<GlobalSQLRunningListener> getRunningListener() {
        return parent.getRunningListener();
    }

    @Override
    public boolean containField(String name) {
        return parent.containField(name);
    }

    @Override
    public boolean isforeignKey(String name) {
        return parent.isforeignKey(name);
    }

    @Override
    public boolean isPrimary(String name) {
        return parent.isPrimary(name);
    }

    @Override
    public String getPrimaryName() {
        return parent.getPrimaryName();
    }


    @Override
    public Connection getConnection() {
        return parent.getConnection();
    }

    @Override
    public TableMetadata getTableMetadata() {
        return parent.getTableMetadata();
    }

    @Override
    public Set<String> getColumnNames() {
        return parent.getColumnNames();
    }

    @Override
    public Set<String> getDataAliases() {
        return parent.getDataAliases();
    }

    @Override
    public String getDatasourceAlias() {
        return parent.getDatasourceAlias();
    }

    @Override
    public AliasColumnConvertHandler getColumnConvertHandler() {
        return parent.getColumnConvertHandler();
    }
}
