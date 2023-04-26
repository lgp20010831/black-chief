package com.black.db;

import com.black.holder.SpringHodler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SpringDBConnection extends AbstractDBConnection{

    private DataSource dataSource;

    public DataSource getDataSource() {
        if (dataSource == null){
            BeanFactory beanFactory = SpringHodler.getBeanFactory();
            dataSource = beanFactory.getBean(DataSource.class);
        }
        return dataSource;
    }

    @Override
    protected Connection getRawConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(getConnection(), getDataSource());
    }

    @Override
    public boolean isTransactionActivity() {
        return !DataSourceUtils.isConnectionTransactional(getConnection(), getDataSource());
    }

    @Override
    public String getName() {
        return "spring";
    }
}
