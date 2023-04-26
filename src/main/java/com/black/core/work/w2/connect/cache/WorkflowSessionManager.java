package com.black.core.work.w2.connect.cache;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class WorkflowSessionManager {

    static SqlSessionFactory sessionFactory;

    private final static ThreadLocal<SqlSession> sessionThreadLocal = new ThreadLocal<>();

    private final static ThreadLocal<WorkflowMapper> mapperThreadLocal = new ThreadLocal<>();

    public static SqlSession openNewSession(){
        Assert.notNull(sessionFactory, "factory 不能为空");
        return sessionFactory.openSession(ExecutorType.SIMPLE);
    }

    public static WorkflowMapper getMapper(){
        Assert.notNull(sessionFactory, "factory 不能为空");
        Configuration configuration = sessionFactory.getConfiguration();
        SqlSession session = sessionThreadLocal.get();
        if (session == null){
            log.info("workflow: 获取新的数据库连接");
            session = openNewSession();
            sessionThreadLocal.set(session);
        }
        WorkflowMapper mapper = mapperThreadLocal.get();
        if (mapper == null){
            mapper = configuration.getMapper(WorkflowMapper.class, session);
            mapperThreadLocal.set(mapper);
        }
        Connection connection = session.getConnection();
        try {
            if (connection.isClosed() || !connection.isValid(2)) {
                log.info("workflow: 数据库连接不可用, 关闭连接");
                session.close();
                sessionThreadLocal.remove();
                mapperThreadLocal.remove();
                return getMapper();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return mapper;
    }
}
