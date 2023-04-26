package com.black.core.work.w2.connect.cache;

import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.MapperRegister;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.datasource.DataSourceBuilderManager;
import com.black.core.sql.code.pattern.ConversionTableNameListener;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;

import java.sql.Connection;

public class MapSqlMapperBuilder {

    private String alias;

    private final WorkflowConfiguration configuration;

    private WorkflowMapMapper workflowMapMapper;

    public MapSqlMapperBuilder(WorkflowConfiguration configuration) {
        this.configuration = configuration;
        MapperRegister register = MapperRegister.getRegister();
        workflowMapMapper = register.getMapper(WorkflowMapMapper.class, globalSQLConfiguration -> {
            alias = globalSQLConfiguration.getDataSourceAlias();
            Class<? extends DataSourceBuilder> dataSourceIfMapSql = configuration.getDataSourceIfMapSql();
            DataSourceBuilder dataSourceBuilder = DataSourceBuilderManager.obtain("workflow_proxy", () -> {
                return DataSourceBuilderTypeManager.getBuilder(dataSourceIfMapSql);
            });
            globalSQLConfiguration.setDataSourceBuilder(dataSourceBuilder);
        });
        register.registerListener(new ConversionTableNameListener());
    }

    public Connection getTempConnection(){
        return ConnectionManagement.getConnection(alias);
    }

    public void closeConnection(Connection connection){
        ConnectionManagement.closeCurrentConnection(alias);
    }

    public WorkflowMapMapper getWorkflowMapMapper() {
        return workflowMapMapper;
    }
}
