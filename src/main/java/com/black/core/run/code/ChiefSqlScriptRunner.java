package com.black.core.run.code;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class ChiefSqlScriptRunner {

    private final Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void runScripts(String... paths){
        for (String path : paths) {
            runScript(path);
        }
    }

    public void runScript(String path){
        if (!StringUtils.hasText(path)){
            return;
        }
        DataSourceBuilder builder = configuration.getDataSourceBuilder();
        boolean allowClose = false;
        Connection connection = configuration.getConnection();
        if (connection == null){
            allowClose = true;
            try {
                connection = builder.getDataSource().getConnection();
            } catch (SQLException e) {
                throw new SQLSException(e);
            }
        }
        //获取文件字节流
        try (InputStream in = configuration.getScanner().getInputStream(configuration, path)) {

            if (in == null){
                if (log.isInfoEnabled()) {
                    log.info("sql file: [{}] is not exist", path);
                }
                return;
            }
            configuration.getExecute().execute(configuration, in, connection);
        } catch (Throwable e) {
            CentralizedExceptionHandling.handlerException(e);
            if (configuration.isStopOnFileError()) {
                if (log.isErrorEnabled()) {
                    log.error("execute file: [{}], has error, stop execute the remaining", path);
                }
                throw new SQLSException(e);
            }
        }finally {
            if (allowClose){
                configuration.getExecute().close(configuration);
            }
        }
    }

    public ChiefSqlScriptRunner() {
        configuration = new Configuration(FactoryManager.initAndGetBeanFactory());
        configuration.setStopOnFileError(false);
        configuration.setStopOnSentenceError(false);
        configuration.setDatasource(SpringDataSourceBuilder.class);
        configuration.setExecuteType(IbtaisSqlExecute.class);
        configuration.setScannerType(ContextSqlScanner.class);
        configuration.lazyLoad();
    }
}
