package com.black.sql;

import com.black.function.Supplier;
import com.black.holder.SpringHodler;
import com.black.ods.OdsUtils;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.function.Consumer;

public class NativeSql {

    public static final AliasColumnConvertHandler DEFAULT_CONVERT_HANDLER = new HumpColumnConvertHandler();

    public static final Log DEFAULT_LOG = new SystemLog();

    public static final String DEFAULT_ALIAS = "master";

    private static BeanFactory beanFactory;

    public static void setBeanFactory(BeanFactory beanFactory) {
        NativeSql.beanFactory = beanFactory;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static QueryResultSetParser query(String sql, Object... paramArray){
        return query(sql, null, paramArray);
    }

    public static QueryResultSetParser envQuery(String sql, Map<String, Object> env, Object... paramArray){
        return envQuery(sql, null, env, paramArray);
    }

    public static QueryResultSetParser query(String sql, Supplier<BeanFactory> supplier, Object... paramArray){
        return envQuery(sql, supplier, null, paramArray);
    }

    public static QueryResultSetParser envQuery(String sql, Supplier<BeanFactory> supplier, Map<String, Object> env, Object... paramArray){
        BeanFactory beanFactory;
        if (supplier != null){
            try {
                beanFactory = supplier.get();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }else {
            beanFactory = getBeanFactory();
            if (beanFactory == null){
                beanFactory = SpringHodler.getBeanFactory();
            }
        }
        if (beanFactory == null){
            throw new IllegalStateException("not find spring beanfactory");
        }
        DataSource bean = beanFactory.getBean(DataSource.class);
        Connection connection = DataSourceUtils.getConnection(bean);
        return createEnvQuery(sql, connection, bean, env, paramArray);
    }

    public static void executeDef(String sql, Object... paramArray){
        execute(sql, DEFAULT_ALIAS, paramArray);
    }

    public static void executeEnvDef(String sql, Map<String, Object> env, Object... paramArray){
        executeEnv(sql, DEFAULT_ALIAS, env, paramArray);
    }

    public static void execute(String sql, String alias, Object... paramArray){
        executeEnv(sql, alias, null, paramArray);
    }

    public static void executeEnv(String sql, String alias, Map<String, Object> env, Object... paramArray){
        ConnectionManagement.employConnection(alias, connection -> {
            executeEnvUpdate(sql, connection, env, paramArray);
        });
    }

    public static void executeUpdateDef(@NonNull String sql, Object... paramArray){
        executeEnvUpdateDef(sql, null, paramArray);
    }

    public static void executeEnvUpdateDef(@NonNull String sql, Map<String, Object> env, Object... paramArray){
        BeanFactory beanFactory = getBeanFactory();
        if (beanFactory == null){
            beanFactory = SpringHodler.getBeanFactory();
        }
        Assert.notNull(beanFactory, "can not find bean factory");
        DataSource dataSource = beanFactory.getBean(DataSource.class);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            executeEnvUpdate(sql, connection, env, paramArray);
        } catch (Throwable e) {
            throw new NativeQueryException(e);
        }finally {
            if (connection != null){
                if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            }
        }
    }

    public static void executeUpdate(@NonNull String sql, Connection connection, Object... paramArray){
        executeEnvUpdate(sql, connection, null, paramArray);
    }

    public static void executeEnvUpdate(@NonNull String sql, Connection connection, Map<String, Object> env, Object... paramArray){
        Map<Object, Object> paramMap = OdsUtils.castParamMap(paramArray);
        if (env != null){
            paramMap.putAll(env);
        }
        sql = Query.doParseSqlWithSeq0(sql, paramMap);
        DEFAULT_LOG.debug("==> execute update sql: " + sql);
        SQLUtils.executeSql(sql, connection);
    }

    public static QueryResultSetParser createQueryDef(@NonNull String sql, Object... paramArray){
        return createQuery(sql, DEFAULT_ALIAS, paramArray);
    }

    public static QueryResultSetParser createQueryEnvDef(@NonNull String sql,  Map<String, Object> env, Object... paramArray){
        return createQueryEnv(sql, DEFAULT_ALIAS, env, paramArray);
    }


    public static QueryResultSetParser createQuery(@NonNull String sql, String alias, Object... paramArray){
        return createQueryEnv(sql, alias, null, paramArray);
    }

    public static QueryResultSetParser createQueryEnv(@NonNull String sql, String alias, Map<String, Object> env, Object... paramArray){
        Query query = NativeQueryManager.createQuery(sql, alias, paramArray);
        query.setLog(DEFAULT_LOG);
        query.setEnv(env);
        QueryResultSetParser parser = query.execute();
        parser.setConvertHandler(DEFAULT_CONVERT_HANDLER);
        return parser;
    }

    public static QueryResultSetParser createQuery(@NonNull String sql, Connection connection, DataSource dataSource, Object... paramArray){
        return createEnvQuery(sql, connection, dataSource, null, paramArray);
    }

    public static QueryResultSetParser createEnvQuery(@NonNull String sql, Connection connection, DataSource dataSource, Map<String, Object> env, Object... paramArray){
        Query query = NativeQueryManager.createQuery(sql, connection, dataSource, paramArray);
        query.setLog(DEFAULT_LOG);
        query.setEnv(env);
        QueryResultSetParser parser = query.execute();
        parser.setConvertHandler(DEFAULT_CONVERT_HANDLER);
        return parser;
    }

    public static QueryResultSetParser createEnvAutonomousShutdownQuery(@NonNull String sql, Connection connection, Consumer<Connection> consumer, Map<String, Object> env, Object... paramArray){
        Query query = NativeQueryManager.createAutonomousShutdownQuery(sql, connection, consumer, paramArray);
        query.setLog(DEFAULT_LOG);
        query.setEnv(env);
        QueryResultSetParser parser = query.execute();
        parser.setConvertHandler(DEFAULT_CONVERT_HANDLER);
        return parser;
    }



}
