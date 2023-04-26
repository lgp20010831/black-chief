package com.black.core.mybatis.source;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Method;

public interface IbatisComponentGiver {


    SqlSessionFactory getSqlSessionFactory(Configuration configuration);


    Configuration getConfiguration(Environment environment);


    MapperMethodWrapper createMapperMethod(Class<?> interfaceClass, Method method, Configuration configuration);

}
