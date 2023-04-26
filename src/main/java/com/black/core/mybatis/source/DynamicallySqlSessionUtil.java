package com.black.core.mybatis.source;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import java.sql.Connection;

@Log4j2
public class DynamicallySqlSessionUtil {


    public static boolean checkSqlSessionVaild(String alias, SqlSession session){
        try {
            Connection connection = session.getConnection();
            return connection != null && connection.isValid(2);
        }catch (Throwable e){
            return false;
        }
    }

    public static SqlSession getSqlSession(String alias, SqlSessionFactory sqlSessionFactory,
                                           ExecutorType executorType,
                                           PersistenceExceptionTranslator exceptionTranslator,
                                           IbatisDataSourceGroupConfigurer configurer){
        SqlSession sqlSession = DynamicallyTransactionManager.getSqlSession(alias);
        if (sqlSession == null){
            sqlSession = sqlSessionFactory.openSession(executorType, configurer.setAutoCommit(alias));
            if (log.isInfoEnabled()) {
                log.info("open sqlSession ... of {}", alias);
            }
            registerSqlsession(alias, sqlSession);
        }else {
            if (!checkSqlSessionVaild(alias, sqlSession)) {
                if (log.isInfoEnabled()) {
                    log.info("sqlSession 已经无效了, 更换新的sqlSession");
                }
                try {
                    closeSession(alias, sqlSession);
                }catch (Throwable e){
                    //ignore
                }
                sqlSession = sqlSessionFactory.openSession(executorType, configurer.setAutoCommit(alias));
                if (log.isInfoEnabled()) {
                    log.info("open new sqlSession ... of {}", alias);
                }
                registerSqlsession(alias, sqlSession);
            }
        }
        return sqlSession;
    }

    public static void registerSqlsession(String alias, SqlSession session){
        DynamicallyTransactionManager.registerSqlSession(alias, session);
    }


    public static void closeSqlSession(String alias, SqlSession session){
        try {

            closeSession(alias, session);
        } catch (Exception e) {
            log.info("close sqlsession error msg: {}", e.getMessage());
        }
    }

    private static void closeSession(String alias, SqlSession session){
        if (log.isInfoEnabled()) {
            log.info("close sqlsession");
        }
        DynamicallyTransactionManager.removeSqlSession(alias);
        session.close();
    }
}
