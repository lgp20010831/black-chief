package com.black.core.mybatis.source;

import com.black.core.builder.Col;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Map;

public interface IbatisDataSourceGroupConfigurer {

    void registerDataSources(DataSourceRegister register);

    default String[] addMapperLocations(){
        return null;
    }

    default String[] addTypeAliasesPackages(){
        return null;
    }

    default Map<String, Class<?>> registerAlias(){
        return Col.of("Timestamp", Timestamp.class);
    }

    default boolean debug(){
        return false;
    }

    default void handlerDataSource(String alias, DataSource dataSource){
    }

    default void handlerConfiguration(String alias, Configuration configuration){}

    default void handlerSqlSessionFactory(String alias, SqlSessionFactory sqlSessionFactory){}

    default boolean setAliveSession(String alias){
        return false;
    }

    default ExecutorType obtainExecutorType(){
        return ExecutorType.SIMPLE;
    }

    default boolean setAutoCommit(String alias){
        return false;
    }

    default String[] getPaths(){
        return new String[0];
    }
}
