package com.black.core.work.w2.connect.cache;

import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Log4j2
public class MybatisMapperBuilder {

    @Setter
    DataSource dataSource;
    //主要数据源
    @Setter
    SqlSessionFactory sqlSessionFactory;

    WorkflowConfiguration configuration;

    public MybatisMapperBuilder(BeanFactory beanFactory, WorkflowConfiguration configuration){
        try {
            this.configuration = configuration;
            this.dataSource = beanFactory.getBean(DataSource.class);
            this.sqlSessionFactory = beanFactory.getBean(SqlSessionFactory.class);
            Configuration mybatisConfig = sqlSessionFactory.getConfiguration();
            mybatisConfig.addMapper(WorkflowMapper.class);
            WorkflowSessionManager.sessionFactory = sqlSessionFactory;
        }catch (BeansException be){
            if (log.isErrorEnabled()) {
                log.error("can not get datasource or sqlSessionFactory");
            }
        }
    }

    public Connection getTempConnection(){
        return DataSourceUtils.getConnection(dataSource);
    }

    public void closeConnection(Connection connection){
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public WorkflowMapper getMapper(){
        return WorkflowSessionManager.getMapper();
    }
}
