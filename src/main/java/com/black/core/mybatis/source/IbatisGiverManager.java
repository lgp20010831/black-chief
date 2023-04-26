package com.black.core.mybatis.source;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.lang.reflect.Method;

public class IbatisGiverManager {

    private static IbatisComponentGiver ibatisComponentGiver;


    public static void registerGiver(IbatisComponentGiver componentGiver){
        IbatisGiverManager.ibatisComponentGiver = componentGiver;
    }


    public static IbatisComponentGiver getIbatisComponentGiver() {

        if (ibatisComponentGiver == null){
            ibatisComponentGiver = new DefaultGiver();
        }
        return ibatisComponentGiver;
    }

    public static class DefaultGiver implements IbatisComponentGiver{


        @Override
        public SqlSessionFactory getSqlSessionFactory(Configuration configuration) {
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            return builder.build(configuration);
        }

        @Override
        public Configuration getConfiguration(Environment environment) {
            return new Configuration(environment);
        }

        @Override
        public MapperMethodWrapper createMapperMethod(Class<?> interfaceClass, Method method, Configuration configuration) {
            return new DefaultMapperMethodWrapper(interfaceClass, method, configuration);
        }

    }


    public static class DefaultMapperMethodWrapper implements MapperMethodWrapper{

        final MapperMethod mapperMethod;

        public DefaultMapperMethodWrapper(Class<?> interfaceClass, Method method, Configuration configuration) {
            mapperMethod = new MapperMethod(interfaceClass, method, configuration);
        }

        @Override
        public Object execute(SqlSession sqlSession, Object[] args) {
            return mapperMethod.execute(sqlSession, args);
        }
    }
}
