package com.black.core.native_sql;

import javax.sql.DataSource;

public class TransactionHandlerAndDataSourceHolder {

    private TransactionalDataSourceHandler transactionalDataSourceHandler;

    private DataSource dataSource;

    private String beanName;

    public TransactionHandlerAndDataSourceHolder(){}

    public TransactionHandlerAndDataSourceHolder(TransactionalDataSourceHandler transactionalDataSourceHandler,
                                                 DataSource dataSource,
                                                 String beanName){
        this.transactionalDataSourceHandler = transactionalDataSourceHandler;
        this.dataSource = dataSource;
        this.beanName = beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTransactionalDataSourceHandler(TransactionalDataSourceHandler transactionalDataSourceHandler) {
        this.transactionalDataSourceHandler = transactionalDataSourceHandler;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public TransactionalDataSourceHandler getTransactionalDataSourceHandler() {
        return transactionalDataSourceHandler;
    }
}
