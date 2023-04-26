package com.black.core.run.code;

import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.run.annotation.EnabledSqlPreExecution;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
@Setter
@IgnorePrint
@LazyLoading(EnabledSqlPreExecution.class)
public class SQLRunnerComponent implements OpenComponent, EnabledControlRisePotential {

    private Configuration configuration;

    public SQLRunnerComponent(){
        System.out.println();
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        if (configuration == null)
            return;
        configuration.lazyLoad();
        String[] position = configuration.getPosition();
        if (position != null && position.length != 0){
            AsynGlobalExecutor.execute(() -> {
                try {
                    for (String path : position) {
                        if (!StringUtils.hasText(path)){
                            continue;
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
                                continue;
                            }
                            configuration.getExecute().execute(configuration, in, connection);
                        } catch (Throwable e) {
                            CentralizedExceptionHandling.handlerException(e);
                            if (configuration.isStopOnFileError()) {
                                if (log.isErrorEnabled()) {
                                    log.error("execute file: [{}], has error, stop execute the remaining", path);
                                }
                                break;
                            }
                        }finally {
                            if (allowClose){
                                configuration.getExecute().close(configuration);
                            }
                        }
                    }
                }finally {
                    try {
                        configuration.getExecute().close(configuration);
                    }catch (Throwable e){}
                }
            });
        }
    }


    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        EnabledSqlPreExecution preExecution = (EnabledSqlPreExecution) annotation;
        FactoryManager.createDefaultBeanFactory();
        configuration = new Configuration(FactoryManager.getBeanFactory());
        AnnotationUtils.loadAttribute(preExecution, configuration);
    }
}
