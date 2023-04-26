package com.black.core.sql.code.config;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.SQLApplicationContext;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.inter.*;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.conformity.ConformityPolicyAllocator;
import com.black.core.sql.conformity.ListSingleConformity;
import com.black.core.sql.conformity.MapAppearanceConformity;
import com.black.core.sql.conformity.StatementConformity;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

@Getter  @Setter
public class GlobalSQLConfiguration {

    private DataSourceBuilder dataSourceBuilder;

    private SQLApplicationContext applicationContext;

    private ThreadLocal<StatementConformity> conformityLocal = new ThreadLocal<>();

    private LinkedBlockingQueue<DefaultSqlStatementCreator> creators = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<SqlsArguramentResolver> arguramentResolvers = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<ConfigurationAnnotationResolver> annotationResolvers = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<SqlValueGroupHandler> groupHandlers = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<AppearanceResolver> appearanceResolvers = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<ExecuteResultResolver> resultResolvers = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<PointSessionExecutor> sessionExecutors = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<PrepareFinishResolver> prepareFinishResolvers = new LinkedBlockingQueue<>();

    private boolean initTable;

    private boolean useStatementCache = false;

    private boolean allowScroll = false;

    private boolean openCondition;

    private Integer batchStrategy = 2000;

    private Log log;

    private String dataSourceAlias;

    private AliasColumnConvertHandler convertHandler;

    public DataSource getDataSource(){
        return dataSourceBuilder.getDataSource();
    }

    public StatementConformity getConformity() {
        StatementConformity conformity = conformityLocal.get();
        if (conformity == null){
            conformity = new ConformityPolicyAllocator();
            conformity.add(new ListSingleConformity());
            conformity.add(new MapAppearanceConformity());
            conformityLocal.set(conformity);
        }
        return conformity;
    }

    public Connection getConnection(){
        return ConnectionManagement.getConnection(getDataSourceAlias());
    }

    public GlobalSQLConfiguration(){
        this(null, null);
    }

    public GlobalSQLConfiguration(DataSourceBuilder dataSourceBuilder, String dataSourceAlias) {
        this.dataSourceBuilder = dataSourceBuilder;
        this.dataSourceAlias = dataSourceAlias;
    }
}
